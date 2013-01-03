package com.atlassian.plugin.remotable.plugin.module.jira.projectconfig;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.ConditionProcessor;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.remotable.spi.module.IFrameViewIssuePanel;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.util.concurrent.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

import java.net.URI;

import static com.atlassian.plugin.remotable.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * A remote project configuration web panel that loads its contents from an iframe.
 */
public class ProjectConfigWebPanelModuleDescriptor extends AbstractModuleDescriptor<Void>
{

    private static final String PROJECT_WEB_PANEL_LOCATION = "webpanels.admin.summary";

    private final IFrameRendererImpl iFrameRenderer;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final HostContainer hostContainer;
    private final BundleContext bundleContext;
    private final ConditionProcessor conditionProcessor;

    private Element descriptor;
    private String weight;
    private URI url;

    private DynamicDescriptorRegistration.Registration registration;
    private String location;

    public ProjectConfigWebPanelModuleDescriptor(final IFrameRendererImpl iFrameRenderer,
            final DynamicDescriptorRegistration dynamicDescriptorRegistration, final HostContainer hostContainer,
            final BundleContext bundleContext, final ConditionProcessor conditionProcessor)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.hostContainer = hostContainer;
        this.bundleContext = bundleContext;
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
        this.location = getLocation(element);
        this.weight = getOptionalAttribute(element, "weight", null);
        this.url = getRequiredUriAttribute(element, "url");
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final String moduleKey = "project-config-panel-" + getRequiredAttribute(descriptor, "key");
        final String panelName = getRequiredAttribute(descriptor, "name");

        Element desc = descriptor.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addAttribute("i18n-key", panelName);
        desc.addAttribute("location", location);
        if (weight != null)
        {
            desc.addAttribute("weight", weight);
        }
        desc.addElement("label").addAttribute("key", panelName);
        desc.addAttribute("class", IFrameViewIssuePanel.class.getName());

        ModuleDescriptor<WebPanel> moduleDescriptor = createWebPanelModuleDescriptor(moduleKey, desc, new IFrameParamsImpl(descriptor));

        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(moduleDescriptor));
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

                    return (T) new IFrameProjectConfigWebPanel(iFrameRenderer, new IFrameContextImpl(getPluginKey(), url, moduleKey, iFrameParams));
                }
            }, getService(bundleContext, WebInterfaceManager.class));

            moduleDescriptor.init(conditionProcessor.getLoadablePlugin(getPlugin()), desc);
            return moduleDescriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }

    private String getLocation(final Element element)
    {
        final String location = getOptionalAttribute(element, "location", null);
        if (StringUtils.isEmpty(location) || location.equals("left-panel"))
        {
            return PROJECT_WEB_PANEL_LOCATION + ".left-panels";
        }
        else if (location.equals("right"))
        {
            return PROJECT_WEB_PANEL_LOCATION + ".right-panels";
        }
        else
        {
            throw new PluginParseException("project-config-panel is missing valid location.");
        }
    }
}
