package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow.ConnectWorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WorkflowPostFunctionResource;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import org.dom4j.Element;

/**
 * A remote post-function module descriptor.
 *
 * TODO delete this when we drop support for XML
 */
public class RemoteWorkflowPostFunctionModuleDescriptor extends ConnectWorkflowFunctionModuleDescriptor
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    public RemoteWorkflowPostFunctionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory, final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler,
            final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry, final EventPublisher eventPublisher,
            final DelegatingComponentAccessor componentAccessor)
    {
        super(authenticationContext, moduleFactory, iFrameRenderStrategyRegistry, jiraRestBeanMarshaler,
                webHookConsumerRegistry, eventPublisher, componentAccessor);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
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
        return super.getModuleClassName();
    }

}
