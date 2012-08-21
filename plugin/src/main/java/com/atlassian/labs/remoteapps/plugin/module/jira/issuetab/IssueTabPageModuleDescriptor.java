package com.atlassian.labs.remoteapps.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.remoteapps.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.labs.remoteapps.plugin.module.IFrameParams;
import com.atlassian.labs.remoteapps.plugin.module.IFrameRenderer;
import com.atlassian.labs.remoteapps.plugin.module.page.IFrameContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * A remote issue tab that loads is contents from an iframe
 */
public class IssueTabPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRenderer iFrameRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private Element descriptor;
    private URI url;

    public IssueTabPageModuleDescriptor(IFrameRenderer iFrameRenderer,
            JiraAuthenticationContext jiraAuthenticationContext,
            DynamicDescriptorRegistration dynamicDescriptorRegistration)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
        this.url = getRequiredUriAttribute(element, "url");
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final String moduleKey = "issue-tab-page-" + getRequiredAttribute(descriptor, "key");
        final String panelName = getRequiredAttribute(descriptor, "name");

        Element desc = descriptor.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(panelName);
        desc.addAttribute("class", IssueTabPage.class.getName());

        IssueTabPanelModuleDescriptor moduleDescriptor = createDescriptor(moduleKey, desc,
                new IFrameParams(descriptor));

        dynamicDescriptorRegistration.registerDescriptors(getPlugin(), moduleDescriptor);
    }

    private IssueTabPanelModuleDescriptor createDescriptor(
            final String moduleKey,
            final Element desc,
            final IFrameParams iFrameParams)
    {
        try
        {
            IssueTabPanelModuleDescriptor descriptor = new FixedIssueTabPanelModuleDescriptor(
                    jiraAuthenticationContext, new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {

                    return (T) new IssueTabPage(
                            new IFrameContext(getPluginKey() , url.toString(), moduleKey, iFrameParams),
                            iFrameRenderer);
                }
            });

            descriptor.init(getPlugin(), desc);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }
}
