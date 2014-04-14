package com.atlassian.plugin.connect.plugin.module.webpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remote project configuration web panel that loads its contents from an iframe.
 */
public class RemoteWebPanelModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public static final String REMOTE_WEB_PANEL_MODULE_PREFIX = "remote-web-panel-";
    private final IFrameRendererImpl iFrameRenderer;
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final HostContainer hostContainer;
    private final BundleContext bundleContext;
    private final ConditionProcessor conditionProcessor;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final UrlValidator urlValidator;

    private String weight;
    private String url;
    private String location;

    private Element descriptor;
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    public RemoteWebPanelModuleDescriptor(
            ModuleFactory moduleFactory,
            IFrameRendererImpl iFrameRenderer,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            HostContainer hostContainer,
            BundleContext bundleContext,
            ConditionProcessor conditionProcessor,
            ContextMapURLSerializer contextMapURLSerializer,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor,
            UrlValidator urlValidator)
    {
        super(moduleFactory);
        this.userManager = checkNotNull(userManager);
        this.contextMapURLSerializer = checkNotNull(contextMapURLSerializer);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.hostContainer = checkNotNull(hostContainer);
        this.bundleContext = checkNotNull(bundleContext);
        this.conditionProcessor = checkNotNull(conditionProcessor);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.urlValidator = urlValidator;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
        this.location = getLocation(element);
        this.weight = getOptionalAttribute(element, "weight", null);
        this.url = getRequiredAttribute(element, "url");
        urlValidator.validate(this.url);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final String moduleKey = REMOTE_WEB_PANEL_MODULE_PREFIX + getRequiredAttribute(descriptor, "key");
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
        desc.addAttribute("class", IFrameRemoteWebPanel.class.getName());
        Condition condition = conditionProcessor.process(descriptor, desc, getPluginKey(), "#" + moduleKey);

        ModuleDescriptor<WebPanel> moduleDescriptor = createWebPanelModuleDescriptor(moduleKey, desc, condition, new IFrameParamsImpl(descriptor));

        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(), new DescriptorToRegister(moduleDescriptor));
    }

    @Override
    public void disabled()
    {
        if (registration != null)
        {
            registration.unregister();
        }
        super.disabled();
    }

    private ModuleDescriptor<WebPanel> createWebPanelModuleDescriptor(
            final String moduleKey,
            final Element desc,
            final Condition condition,
            final IFrameParams iFrameParams)
    {
        try
        {
            ModuleDescriptor<WebPanel> moduleDescriptor = new DefaultWebPanelModuleDescriptor(hostContainer, new ModuleFactory()
            {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {
                    return (T) new IFrameRemoteWebPanel(
                            iFrameRenderer,
                            new IFrameContextImpl(getPluginKey(), url, moduleKey, iFrameParams),
                            condition != null ? condition : new AlwaysDisplayCondition(),
                            contextMapURLSerializer, userManager, urlVariableSubstitutor);
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

    @Override
    public Void getModule()
    {
        return null;
    }

    protected String getLocation(final Element element)
    {
        return getRequiredAttribute(element, "location");
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
