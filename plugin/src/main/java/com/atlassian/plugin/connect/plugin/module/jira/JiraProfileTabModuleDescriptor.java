package com.atlassian.plugin.connect.plugin.module.jira;

import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.spi.module.IFrameViewProfilePanel;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a user profile tab with a servlet containing an iframe and a web item
 */
public class JiraProfileTabModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IFrameRendererImpl iFrameRenderer;
    private Element descriptor;
    private URI url;
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    public JiraProfileTabModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            JiraAuthenticationContext jiraAuthenticationContext,
            IFrameRendererImpl iFrameRenderer)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
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

        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(
                createProfilePanelDescriptor(descriptor, getKey(), url)));
    }

    @Override
    public void disabled()
    {
        super.disabled();
        if (registration != null)
        {
            registration.unregister();
        }
    }

    private ViewProfilePanelModuleDescriptor createProfilePanelDescriptor(
            final Element e,
            String key,
            final URI path
    )
    {
        final String panelName = getRequiredAttribute(e, "name");
        Element config = e.createCopy();
        final String moduleKey = "profile-" + key;
        config.addAttribute("key", moduleKey);
        config.addAttribute("system", "true");
        config.addAttribute("i18n-key", panelName);
        config.addAttribute("class", IFrameViewProfilePanel.class.getName());

        final ViewProfilePanelModuleDescriptor descriptor = new ViewProfilePanelModuleDescriptorImpl(jiraAuthenticationContext, new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                return (T) new IFrameViewProfilePanel(
                        iFrameRenderer,
                        new IFrameContextImpl(getPluginKey(), path, moduleKey, new IFrameParamsImpl(e)));
            }
        });
        descriptor.init(getPlugin(), config);
        return descriptor;
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
