package com.atlassian.plugin.connect.jira.workflow;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.DelegatingComponentAccessor;
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
    private String addonKey;

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
        this.addonKey = addonKeyOnly(getKey());
    }

    @Override
    public void enabled()
    {
        super.enabled();
        webHookConsumerRegistry.register(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                addonKey,
                triggeredUri,
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(getKey()), ImmutableMap.<String, Object>of(),
                        RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
    }

    @Override
    public void disabled()
    {
        webHookConsumerRegistry.unregister(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                addonKey,
                triggeredUri,
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(getKey()), ImmutableMap.<String, Object>of(),
                        RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );

        super.disabled();
    }

    @Override
    public void writeHtml(String resourceName, Map<String, ?> startingParams, Writer writer) throws IOException
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()), resourceName);

        if (renderStrategy.shouldShow(Collections.emptyMap()))
        {
            Map<String, String> contextParameters = ImmutableMap.of(
                    WorkflowPostFunctionContextParameters.POSTFUNCTION_ID, (String) startingParams.get(WorkflowPostFunctionContextParameters.POSTFUNCTION_ID),
                    WorkflowPostFunctionContextParameters.POSTFUNCTION_CONFIG, (String) startingParams.get(WorkflowPostFunctionContextParameters.POSTFUNCTION_CONFIG)
            );
            renderStrategy.render(contextParameters, writer, java.util.Optional.empty());
        }
        else
        {
            renderStrategy.renderAccessDenied(writer);
        }
    }
}
