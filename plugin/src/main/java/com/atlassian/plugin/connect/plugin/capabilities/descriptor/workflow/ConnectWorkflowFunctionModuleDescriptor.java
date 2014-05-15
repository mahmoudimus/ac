package com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow;

import com.atlassian.event.api.EventPublisher;
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
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionProvider;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.WorkflowException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(ConnectWorkflowFunctionModuleDescriptor.class);

    public static final String TRIGGERED_URL = "triggeredUrl";

    private final TypeResolver remoteWorkflowTypeResolver;
    private final OSWorkflowConfigurator workflowConfigurator;
    private final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private String addonKey;
    private String moduleKey;

    private URI triggeredUri;

    public ConnectWorkflowFunctionModuleDescriptor(
            final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory,
            final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler,
            final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry,
            final EventPublisher eventPublisher,
            final DelegatingComponentAccessor componentAccessor)
    {
        super(authenticationContext, componentAccessor.getComponent(OSWorkflowConfigurator.class),
                componentAccessor.getComponent(ComponentClassManager.class), moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webHookConsumerRegistry = checkNotNull(webHookConsumerRegistry);
        this.workflowConfigurator = checkNotNull(componentAccessor.getComponent(OSWorkflowConfigurator.class));

        this.remoteWorkflowTypeResolver = new TypeResolver()
        {
            @Override
            public FunctionProvider getFunction(final String type, final Map args) throws WorkflowException
            {
                return new RemoteWorkflowPostFunctionProvider(eventPublisher, jiraRestBeanMarshaler, addonKeyOnly(getKey()), getKey());
            }
        };
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
        //TODO: This should not be tied to the lifecycle of the add-on instance
        workflowConfigurator.registerTypeResolver(RemoteWorkflowPostFunctionProvider.class.getName(), remoteWorkflowTypeResolver);
        webHookConsumerRegistry.register(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                triggeredUri,
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(getKey()), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
    }

    @Override
    public void disabled()
    {
        //TODO: This should not be tied to the lifecycle of the add-on instance
        if(null != remoteWorkflowTypeResolver)
        {
            workflowConfigurator.unregisterTypeResolver(RemoteWorkflowPostFunctionProvider.class.getName(), remoteWorkflowTypeResolver);
        }
        
        webHookConsumerRegistry.unregister(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                triggeredUri,
                new PluginModuleListenerParameters(addonKeyOnly(getKey()), Optional.of(getKey()), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
        
        //need to wrap since the pluginTypeResolver may have already gone away
        
        try
        {
            super.disabled();
        }
        catch (Exception e)
        {
            //ignore
        }
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

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}
