package com.atlassian.plugin.remotable.plugin.module.jira.projecttab;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
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
import com.atlassian.plugin.remotable.plugin.util.node.Dom4jNode;
import com.atlassian.plugin.remotable.plugin.util.node.Node;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.dom4j.DocumentHelper.createElement;

/**
 * A remote project tab that loads is contents from an iframe
 */
public final class ProjectTabPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRendererImpl iFrameRenderer;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConditionProcessor conditionProcessor;
    private Node descriptor;
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
        this.descriptor = new Dom4jNode(element);
        this.url = descriptor.get("url").asURI();
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final String panelName = descriptor.get("name").asString();

        Element desc = createElement("project-tab-panel");
        String moduleKey = "project-tab-" + descriptor.get("key").asString();
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
            desc.addAttribute("system", "true");
            ProjectTabPanelModuleDescriptor descriptor = new ProjectTabPanelModuleDescriptorImpl(
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
