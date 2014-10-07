package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow.ConnectWorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WorkflowPostFunctionResource;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.*;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.webhooks.api.register.listener.ModuleDescriptorWebHookListenerRegistry;
import org.dom4j.Element;

/**
 * A remote post-function module descriptor.
 * <p/>
 * TODO delete this when we drop support for XML
 */
@XmlDescriptor
public class RemoteWorkflowPostFunctionModuleDescriptor extends ConnectWorkflowFunctionModuleDescriptor
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    public RemoteWorkflowPostFunctionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory, final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry,
            final DelegatingComponentAccessor componentAccessor)
    {
        super(authenticationContext, moduleFactory, iFrameRenderStrategyRegistry,
                webHookConsumerRegistry, componentAccessor);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        XmlDescriptorExploder.notifyAndExplode(null == plugin ? null : plugin.getKey());

        super.init(plugin, element);

        registerStrategy(WorkflowPostFunctionResource.VIEW, element.element("view"));
        registerStrategy(WorkflowPostFunctionResource.CREATE, element.element("create"));
        registerStrategy(WorkflowPostFunctionResource.EDIT, element.element("edit"));
    }

    private void registerStrategy(WorkflowPostFunctionResource view, final Element urlElement)
    {
        if (urlElement == null)
        {
            return;
        }

        IFrameRenderStrategy strategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(getPluginKey())
                .module(getKey())
                .workflowPostFunctionTemplate(view)
                .urlTemplate(urlElement.attributeValue("url"))
                .build();

        iFrameRenderStrategyRegistry.register(getPluginKey(), getKey(), view.getResource(), strategy);
    }

    @Override
    public String getModuleClassName()
    {
        XmlDescriptorExploder.notifyAndExplode(getPluginKey());

        return super.getModuleClassName();
    }

}
