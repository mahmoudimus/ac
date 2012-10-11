package com.atlassian.plugin.remotable.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.ConditionProcessor;
import com.atlassian.plugin.remotable.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.remotable.plugin.module.IFrameParams;
import com.atlassian.plugin.remotable.plugin.module.IFrameRenderer;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * A remote issue tab that loads is contents from an iframe
 */
public class IssueTabPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRenderer iFrameRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConditionProcessor conditionProcessor;
    private Element descriptor;
    private URI url;

    public IssueTabPageModuleDescriptor(IFrameRenderer iFrameRenderer,
            JiraAuthenticationContext jiraAuthenticationContext,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            ConditionProcessor conditionProcessor)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.conditionProcessor = conditionProcessor;
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
        final String panelName = getRequiredAttribute(descriptor, "name");

        Element desc = descriptor.createCopy();

        String moduleKey = "issue-tab-page-" + getRequiredAttribute(descriptor, "key");

        // make sure to update remote-condition.js to hide these
        Condition condition = conditionProcessor.process(descriptor, desc, getPluginKey(), "#" + moduleKey + "-remote-condition");
        if (condition instanceof ContainingRemoteCondition)
        {
            moduleKey += "-remote-condition";
        }
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(panelName);
        desc.addAttribute("class", IssueTabPage.class.getName());

        IssueTabPanelModuleDescriptor moduleDescriptor = createDescriptor(moduleKey, desc,
                new IFrameParams(descriptor), condition);

        dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(moduleDescriptor));
    }

    private IssueTabPanelModuleDescriptor createDescriptor(
            final String moduleKey,
            final Element desc,
            final IFrameParams iFrameParams, final Condition condition)
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
                            new IFrameContext(getPluginKey() , url, moduleKey, iFrameParams),
                            iFrameRenderer, condition);
                }
            });

            descriptor.init(conditionProcessor.getLoadablePlugin(getPlugin()), desc);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }
}
