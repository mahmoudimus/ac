package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionProvider;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.*;
import static com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION;
import static com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION_UUID;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A ModuleDescriptor for Connect's version of a Jira Workflow Post Function.
 */
public class ConnectWorkflowFunctionModuleDescriptor extends WorkflowFunctionModuleDescriptor
{
    public static final String TRIGGERED_URL = "triggeredUrl";
    private static final Logger log = LoggerFactory.getLogger(ConnectWorkflowFunctionModuleDescriptor.class);

    private static final String POST_FUNCTION_EXTRA_MARKUP = "velocity/jira/workflow/post-function-extra-markup.vm";

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final TypeResolver remoteWorkflowTypeResolver;
    private final IFrameRenderer iFrameRenderer;
    private final TemplateRenderer templateRenderer;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final PluginRetrievalService pluginRetrievalService;
    private final OSWorkflowConfigurator workflowConfigurator;
    private final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry;
    private final UrlValidator urlValidator;

    private URI triggeredUri;

    public ConnectWorkflowFunctionModuleDescriptor(
            final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory,
            final IFrameRenderer iFrameRenderer,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler,
            final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry,
            final EventPublisher eventPublisher,
            final TemplateRenderer templateRenderer,
            final WebResourceUrlProvider webResourceUrlProvider,
            final PluginRetrievalService pluginRetrievalService,
            final UrlValidator urlValidator,
            final DelegatingComponentAccessor componentAccessor)
    {
        super(authenticationContext, componentAccessor.getComponent(OSWorkflowConfigurator.class),
                componentAccessor.getComponent(ComponentClassManager.class), moduleFactory);

        this.jiraAuthenticationContext = checkNotNull(authenticationContext);
        this.webHookConsumerRegistry = checkNotNull(webHookConsumerRegistry);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.webResourceUrlProvider = checkNotNull(webResourceUrlProvider);
        this.pluginRetrievalService = checkNotNull(pluginRetrievalService);
        this.urlValidator = checkNotNull(urlValidator);
        this.workflowConfigurator = checkNotNull(componentAccessor.getComponent(OSWorkflowConfigurator.class));

        this.remoteWorkflowTypeResolver = new TypeResolver()
        {
            @Override
            public FunctionProvider getFunction(final String type, final Map args) throws WorkflowException
            {
                return new RemoteWorkflowPostFunctionProvider(eventPublisher, jiraRestBeanMarshaler, plugin.getKey(), getKey());
            }
        };
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        validateElement(element);

        this.triggeredUri = URI.create(element.attributeValue(TRIGGERED_URL));
    }

    private void validateElement(Element element) throws PluginParseException
    {
        try
        {
            urlValidator.validate(element.attributeValue(TRIGGERED_URL));
            validateResourceLocation(RESOURCE_NAME_VIEW);
            validateResourceLocation(RESOURCE_NAME_EDIT_PARAMETERS);
            validateResourceLocation(RESOURCE_NAME_INPUT_PARAMETERS);
        }
        catch (IllegalArgumentException e)
        {
            throw new PluginParseException(e);
        }
    }

    private void validateResourceLocation(String resourceName)
    {
        ResourceDescriptor resource = getResourceDescriptor(RESOURCE_TYPE_VELOCITY, resourceName);
        if (null != resource)
        {
            urlValidator.validate(resource.getLocation());
        }
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
                getTriggeredUri(),
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(getKey()), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
    }

    @Override
    public void disabled()
    {
        //TODO: This should not be tied to the lifecycle of the add-on instance
        workflowConfigurator.unregisterTypeResolver(RemoteWorkflowPostFunctionProvider.class.getName(), remoteWorkflowTypeResolver);
        webHookConsumerRegistry.unregister(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                getTriggeredUri(),
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(getKey()), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
        super.disabled();
    }

    @Override
    public void writeHtml(String resourceName, Map<String, ?> startingParams, Writer writer) throws IOException
    {
        writer.write(renderIFrame(startingParams, resourceName));
    }

    private String renderIFrame(Map<String, ?> params, String resourceName)
    {
        try
        {
            ApplicationUser user = jiraAuthenticationContext.getUser();
            Preconditions.checkNotNull(user);
            UUID uuid = checkValidUUID((String) params.get(POST_FUNCTION_CONFIGURATION_UUID));
            IFrameContext iFrameContext = createIFrameContext(resourceName, params, uuid);
            return iFrameRenderer.render(
                    iFrameContext,
                    "",
                    ImmutableMap.of(POST_FUNCTION_CONFIGURATION_UUID, new String[]{uuid.toString()}),
                    user.getUsername(),
                    Collections.<String, Object>emptyMap());
        }
        catch (IOException e)
        {
            log.warn("Could not render iFrame", e);
        }
        catch (IllegalArgumentException e)
        {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    private IFrameContextImpl createIFrameContext(String resourceName, Map<String, ?> params, UUID uuid) throws IOException
    {
        IFrameParams iFrameParams = createIFrameParams(params, uuid);
        String namespace = getKey() + uuid;
        ResourceDescriptor resource = getResourceDescriptor(RESOURCE_TYPE_VELOCITY, resourceName);
        if (null == resource)
        {
            throw new IllegalArgumentException("Check the XML initialization element. No resource found for: " + resourceName);
        }
        if (null == resource.getLocation())
        {
            throw new IllegalArgumentException("Check the XML initialization element. No location registered for: " + resourceName);
        }
        return new IFrameContextImpl(getPluginKey(), resource.getLocation(), namespace, iFrameParams);
    }

    private IFrameParams createIFrameParams(final Map<String, ?> params, final UUID uuid)
            throws IOException
    {
        String functionConfiguration = StringUtils.defaultString((String) params.get(POST_FUNCTION_CONFIGURATION));
        Map<String, Object> extraMarkupParams = ImmutableMap.<String, Object>of(
                POST_FUNCTION_CONFIGURATION, functionConfiguration,
                POST_FUNCTION_CONFIGURATION_UUID, uuid.toString(),
                "scriptUrl", getDialogScriptUrl()
        );
        // Render the extra markup containing configuration value and descriptor uuid.
        StringWriter writer = new StringWriter();
        templateRenderer.render(POST_FUNCTION_EXTRA_MARKUP, extraMarkupParams, writer);

        IFrameParamsImpl iFrameParams = new IFrameParamsImpl();
        iFrameParams.setParamNoEscape("extraMarkupHtml", writer.toString());
        return iFrameParams;
    }

    private String getDialogScriptUrl()
    {
        ArrayList<String> scriptUrls = newArrayList();
        ModuleDescriptor<?> dialogModuleDescriptor = pluginRetrievalService.getPlugin().getModuleDescriptor("dialog");
        for (ResourceDescriptor descriptor : dialogModuleDescriptor.getResourceDescriptors())
        {
            String src = webResourceUrlProvider.getStaticPluginResourceUrl(dialogModuleDescriptor, descriptor.getName(), UrlMode.AUTO);
            if (src.endsWith("js"))
            {
                scriptUrls.add(src);
            }
        }
        if (scriptUrls.size() > 1)
        {
            log.warn("Expected only one static plugin resource URL, but found " + scriptUrls.size());
        }
        return scriptUrls.isEmpty() ? null : scriptUrls.get(0);
    }

    private static UUID checkValidUUID(String uuid)
    {
        if (null == uuid)
        {
            throw new IllegalArgumentException("UUID must not be null");
        }
        return UUID.fromString(uuid);
    }

    private URI getTriggeredUri()
    {
        return triggeredUri;
    }
}
