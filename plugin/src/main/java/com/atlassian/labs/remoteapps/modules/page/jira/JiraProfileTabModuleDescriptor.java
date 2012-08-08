package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.remoteapps.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.labs.remoteapps.spi.modules.IFrameViewProfilePanel;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * Generates a user profile tab with a servlet containing an iframe and a web item
 */
public class JiraProfileTabModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IFrameRenderer iFrameRenderer;
    private Element descriptor;
    private String url;

    public JiraProfileTabModuleDescriptor(
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            JiraAuthenticationContext jiraAuthenticationContext, IFrameRenderer iFrameRenderer)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.iFrameRenderer = iFrameRenderer;
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
        this.url = getRequiredUriAttribute(element, "url").toString();
    }

    @Override
    public void enabled()
    {
        super.enabled();

        dynamicDescriptorRegistration.registerDescriptors(getPlugin(), ImmutableSet.<ModuleDescriptor>of(
                createProfilePanelDescriptor(descriptor, getKey(), url)));
    }

    private ViewProfilePanelModuleDescriptor createProfilePanelDescriptor(
            final Element e,
            String key,
            final String path
    )
    {
        final String panelName = getRequiredAttribute(e, "name");
        Element config = e.createCopy();
        final String moduleKey = "profile-" + key;
        config.addAttribute("key", moduleKey);
        config.addAttribute("i18n-key", panelName);
        config.addAttribute("class", IFrameViewProfilePanel.class.getName());

        final ViewProfilePanelModuleDescriptor descriptor = new ViewProfilePanelModuleDescriptorImpl(jiraAuthenticationContext, new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                return (T) new IFrameViewProfilePanel(
                        iFrameRenderer,
                        new IFrameContext(getPluginKey(), path, moduleKey, new IFrameParams(e)));
            }
        });
        descriptor.init(getPlugin(), config);
        return descriptor;
    }
}
