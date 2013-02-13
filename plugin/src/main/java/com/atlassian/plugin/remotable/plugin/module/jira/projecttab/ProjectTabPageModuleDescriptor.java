package com.atlassian.plugin.remotable.plugin.module.jira.projecttab;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.ConditionProcessor;
import com.atlassian.plugin.remotable.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remote project tab that loads is contents from an iframe
 */
public final class ProjectTabPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRendererImpl iFrameRenderer;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConditionProcessor conditionProcessor;
    private Element descriptor;
    private URI url;
    private DynamicDescriptorRegistration.Registration registration;

    public ProjectTabPageModuleDescriptor(
            ModuleFactory moduleFactory,
            IFrameRendererImpl iFrameRenderer,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            ConditionProcessor conditionProcessor)
    {
        super(moduleFactory);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
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
        this.url = getRequiredUriAttribute(element, "url");
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final String panelName = getRequiredAttribute(descriptor, "name");

        Element desc = descriptor.createCopy();
        String moduleKey = "project-tab-" + getRequiredAttribute(descriptor, "key");
        Condition condition = conditionProcessor.process(descriptor, desc, getPluginKey(), "#" + moduleKey + "-remote-condition-panel");
        if (condition instanceof ContainingRemoteCondition)
        {
            moduleKey += "-remote-condition";
        }
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(panelName);
        desc.addAttribute("class", IFrameProjectTab.class.getName());

        ProjectTabPanelModuleDescriptor moduleDescriptor = createDescriptor(moduleKey,
                desc, new IFrameParamsImpl(descriptor), condition);
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

    private ProjectTabPanelModuleDescriptor createDescriptor(
            final String key,
            final Element desc,
            final IFrameParams iFrameParams, final Condition condition)
    {
        try
        {
            ProjectTabPanelModuleDescriptor descriptor = new FixedProjectTabPanelModuleDescriptor(
                    ComponentManager.getComponent(JiraAuthenticationContext.class), new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {

                    return (T) new IFrameProjectTab(
                            new IFrameContextImpl(getPluginKey() , url, key, iFrameParams),
                            iFrameRenderer, condition);
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
