package com.atlassian.labs.remoteapps.plugin.module.jira.issuepanel;

import com.atlassian.labs.remoteapps.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.labs.remoteapps.plugin.module.IFrameParams;
import com.atlassian.labs.remoteapps.plugin.module.IFrameRenderer;
import com.atlassian.labs.remoteapps.spi.module.IFrameViewIssuePanel;
import com.atlassian.labs.remoteapps.plugin.module.page.IFrameContext;
import com.atlassian.labs.remoteapps.spi.module.IFrameViewProfilePanel;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * A view issue panel page that loads is contents from an iframe
 */
public class IssuePanelPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRenderer iFrameRenderer;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final HostContainer hostContainer;
    private final WebInterfaceManager webInterfaceManager;
    private Element descriptor;
    private URI url;

    public IssuePanelPageModuleDescriptor(IFrameRenderer iFrameRenderer,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            HostContainer hostContainer, WebInterfaceManager webInterfaceManager)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.hostContainer = hostContainer;
        this.webInterfaceManager = webInterfaceManager;
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
        final String moduleKey = "issue-panel-" + getRequiredAttribute(descriptor, "key");
        final String panelName = getRequiredAttribute(descriptor, "name");

        Element desc = descriptor.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addAttribute("i18n-key", panelName);
        desc.addAttribute("class", IFrameViewProfilePanel.class.getName());
        desc.addAttribute("location", "atl.jira.view.issue.right.context");
        desc.addAttribute("class", IFrameViewIssuePanel.class.getName());

        ModuleDescriptor<WebPanel> moduleDescriptor = createWebPanelModuleDescriptor(moduleKey, desc, new IFrameParams(descriptor));

        dynamicDescriptorRegistration.registerDescriptors(getPlugin(), moduleDescriptor);
    }
    private ModuleDescriptor<WebPanel> createWebPanelModuleDescriptor(
            final String moduleKey,
            final Element desc,
            final IFrameParams iFrameParams)
    {
        try
        {
            ModuleDescriptor<WebPanel> moduleDescriptor = new DefaultWebPanelModuleDescriptor(hostContainer, new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {

                    return (T) new IFrameViewIssuePanel(
                            iFrameRenderer,
                            new IFrameContext(getPluginKey(), url.toString(), moduleKey, iFrameParams));
                }
            }, webInterfaceManager);

            moduleDescriptor.init(getPlugin(), desc);
            return moduleDescriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }
}
