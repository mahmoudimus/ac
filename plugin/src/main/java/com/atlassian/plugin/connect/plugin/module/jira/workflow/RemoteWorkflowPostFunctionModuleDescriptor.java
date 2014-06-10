package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow.ConnectWorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
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
        super.init(plugin, element);

        registerStrategy(JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW, element.element("view"));
        registerStrategy(JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS, element.element("create"));
        registerStrategy(JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS, element.element("edit"));
    }

    private void registerStrategy(final String classifier, final Element urlElement)
    {
        if (urlElement == null)
        {
            return;
        }

        IFrameRenderStrategy strategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(getPluginKey())
                .module(getKey())
                .workflowPostFunctionTemplate()
                .urlTemplate(urlElement.attributeValue("url"))
                .build();

        iFrameRenderStrategyRegistry.register(getPluginKey(), getKey(), classifier, strategy);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}
