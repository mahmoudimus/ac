package com.atlassian.plugin.remotable.plugin.module.jira.issuepanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.ConditionProcessor;
import com.atlassian.plugin.remotable.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.remotable.spi.module.IFrameViewIssuePanel;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.atlassian.plugin.remotable.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A view issue panel page that loads is contents from an iframe
 */
public final class IssuePanelPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRendererImpl iFrameRenderer;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final HostContainer hostContainer;
    private final BundleContext bundleContext;
    private final ConditionProcessor conditionProcessor;
    private Element descriptor;
    private String weight;
    private String location = "atl.jira.view.issue.right.context";
    private URI url;

    private final static Logger log = LoggerFactory.getLogger(IssuePanelPageModuleDescriptor.class);
    private DynamicDescriptorRegistration.Registration registration;

    public IssuePanelPageModuleDescriptor(
            ModuleFactory moduleFactory,
            IFrameRendererImpl iFrameRenderer,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            HostContainer hostContainer,
            BundleContext bundleContext,
            ConditionProcessor conditionProcessor)
    {
        super(moduleFactory);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.hostContainer = checkNotNull(hostContainer);
        this.bundleContext = checkNotNull(bundleContext);
        this.conditionProcessor = checkNotNull(conditionProcessor);
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
        this.location = getOptionalAttribute(element, "location", location);
        this.weight = getOptionalAttribute(element, "weight", null);
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
        desc.addAttribute("location", location);
        if (weight != null)
        {
            desc.addAttribute("weight", weight);
        }
        desc.addElement("label").addAttribute("key", panelName);
        desc.addAttribute("class", IFrameViewIssuePanel.class.getName());
        Condition condition = conditionProcessor.process(descriptor, desc, getPluginKey(), "#" + moduleKey);
        log.debug("generated web panel: " + printNode(desc));

        ModuleDescriptor<WebPanel> moduleDescriptor = createWebPanelModuleDescriptor(moduleKey, desc, condition, new IFrameParamsImpl(descriptor));

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
            final Condition condition,
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
                            new IFrameContextImpl(getPluginKey(), url, moduleKey, iFrameParams), condition instanceof ContainingRemoteCondition);
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
}
