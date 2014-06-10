package com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow;

import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Element;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A ModuleDescriptor for Connect's version of a Jira Workflow Post Function.
 */
public class ConnectWorkflowFunctionModuleDescriptor extends WorkflowFunctionModuleDescriptor
{
    public static final String TRIGGERED_URL = "triggeredUrl";

    private final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private URI triggeredUri;

    public ConnectWorkflowFunctionModuleDescriptor(
            final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory,
            final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry,
            final DelegatingComponentAccessor componentAccessor)
    {
        super(authenticationContext, componentAccessor.getComponent(OSWorkflowConfigurator.class),
                componentAccessor.getComponent(ComponentClassManager.class), moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webHookConsumerRegistry = checkNotNull(webHookConsumerRegistry);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.triggeredUri = URI.create(element.attributeValue(TRIGGERED_URL));
    }

    @Override
    public void enabled()
    {
        super.enabled();
        webHookConsumerRegistry.register(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                triggeredUri,
                new PluginModuleListenerParameters(addonKeyOnly(getKey()), Optional.of(getKey()), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
    }

    @Override
    public void disabled()
    {
        webHookConsumerRegistry.unregister(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                triggeredUri,
                new PluginModuleListenerParameters(addonKeyOnly(getKey()), Optional.of(getKey()), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );

        super.disabled();
    }

    @Override
    public void writeHtml(String resourceName, Map<String, ?> startingParams, Writer writer) throws IOException
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()), resourceName);

        if (renderStrategy.shouldShow(Collections.<String, Object>emptyMap()))
        {
            ModuleContextParameters moduleContext = new JiraModuleContextParametersImpl();
            moduleContext.put(
                    JiraModuleContextFilter.POSTFUNCTION_ID,
                    (String) startingParams.get(JiraModuleContextFilter.POSTFUNCTION_ID)
            );
            moduleContext.put(
                    JiraModuleContextFilter.POSTFUNCTION_CONFIG,
                    (String) startingParams.get(JiraModuleContextFilter.POSTFUNCTION_CONFIG)
            );
            renderStrategy.render(moduleContext, writer, Option.<String>none());
        }
        else
        {
            renderStrategy.renderAccessDenied(writer);
        }
    }
}
