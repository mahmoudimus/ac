package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
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
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.*;
import static com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION;
import static com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION_UUID;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;

/**
 * A remote post-function module descriptor.
 */
public class RemoteWorkflowPostFunctionModuleDescriptor extends WorkflowFunctionModuleDescriptor
{
    private static final String POST_FUNCTION_EXTRA_MARKUP = "velocity/jira/workflow/post-function-extra-markup.vm";

    private final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry;
    private final OSWorkflowConfigurator workflowConfigurator;
    private final IFrameRenderer iFrameRenderer;
    private final TypeResolver remoteWorkflowTypeResolver;
    private final TemplateRenderer templateRenderer;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final PluginRetrievalService pluginRetrievalService;

    private Element descriptor;
    private Map<String, URI> workflowFunctionActionUris;
    private String moduleKey;
    private URI publishURI;

    public RemoteWorkflowPostFunctionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory,
            final IFrameRenderer iFrameRenderer,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler,
            final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry,
            final EventPublisher eventPublisher,
            final TemplateRenderer templateRenderer,
            final WebResourceUrlProvider webResourceUrlProvider,
            final PluginRetrievalService pluginRetrievalService)
    {
        super(authenticationContext,
                ComponentAccessor.getComponent(OSWorkflowConfigurator.class),
                ComponentAccessor.getComponent(ComponentClassManager.class),
                moduleFactory);

        this.webHookConsumerRegistry = webHookConsumerRegistry;
        this.templateRenderer = templateRenderer;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.pluginRetrievalService = pluginRetrievalService;
        this.workflowConfigurator = ComponentAccessor.getComponent(OSWorkflowConfigurator.class);
        this.iFrameRenderer = iFrameRenderer;

        this.remoteWorkflowTypeResolver = new TypeResolver()
        {
            @Override
            public FunctionProvider getFunction(final String type, final Map args) throws WorkflowException
            {
                return new RemoteWorkflowPostFunctionProvider(eventPublisher, jiraRestBeanMarshaler, plugin.getKey(), moduleKey);
            }
        };
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        try
        {
            element.addElement(getParameterName()).addText(RemoteWorkflowPostFunctionProvider.class.getName());
            this.descriptor = element;
            this.moduleKey = getRequiredAttribute(element, "key");
            this.publishURI = createURI(element);

            final ImmutableMap.Builder<String, URI> workflowFunctionActionUrisMapBuilder = ImmutableMap.builder();
            if (descriptor.element("view") != null)
            {
                workflowFunctionActionUrisMapBuilder.put(RESOURCE_NAME_VIEW, iFrameURI(RESOURCE_NAME_VIEW));
            }
            if (descriptor.element("create") != null)
            {
                workflowFunctionActionUrisMapBuilder.put(RESOURCE_NAME_INPUT_PARAMETERS, iFrameURI(RESOURCE_NAME_INPUT_PARAMETERS));
            }
            if (descriptor.element("edit") != null)
            {
                workflowFunctionActionUrisMapBuilder.put(RESOURCE_NAME_EDIT_PARAMETERS, iFrameURI(RESOURCE_NAME_EDIT_PARAMETERS));
            }
            this.workflowFunctionActionUris = workflowFunctionActionUrisMapBuilder.build();

            super.init(plugin, element);
            // Resources have to be initialized after super.init is executed, otherwise super.init will set the
            // resources to null.
            this.resources = createResourceDescriptors(workflowFunctionActionUris);
        }
        catch (URISyntaxException e)
        {
            throw new PluginParseException(e);
        }
    }

    @Override
    public void enabled()
    {
        workflowConfigurator.registerTypeResolver(RemoteWorkflowPostFunctionProvider.class.getName(), remoteWorkflowTypeResolver);
        this.webHookConsumerRegistry.register(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                publishURI,
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(moduleKey), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
    }

    @Override
    public void disabled()
    {
        workflowConfigurator.unregisterTypeResolver(RemoteWorkflowPostFunctionProvider.class.getName(), remoteWorkflowTypeResolver);
        this.webHookConsumerRegistry.unregister(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                plugin.getKey(),
                publishURI,
                new PluginModuleListenerParameters(plugin.getKey(), Optional.of(moduleKey), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
        );
    }

    @Override
    public void destroy(final Plugin plugin)
    {
        disabled();
    }

    @Override
    public String getHtml(final String resourceName, @Nullable final AbstractDescriptor functionDescriptor)
    {
        try
        {
            final Map<String, ?> params = getModule().getVelocityParams(resourceName, functionDescriptor);
            final String uuid = (String) params.get(POST_FUNCTION_CONFIGURATION_UUID);
            final IFrameParams iFrameParams = createIFrameParams(params, uuid);
            final String namespace = moduleKey + uuid;
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
            String username = user == null ? "" : user.getUsername();

            return iFrameRenderer.render(
                    new IFrameContextImpl(getPluginKey(),
                            workflowFunctionActionUris.get(resourceName),
                            namespace,
                            iFrameParams),
                    "",
                    ImmutableMap.of(POST_FUNCTION_CONFIGURATION_UUID, new String[] { uuid }),
                    username,
                    Collections.<String, Object>emptyMap());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private IFrameParams createIFrameParams(final Map<String, ?> params, final String uuid)
            throws IOException
    {
        final IFrameParamsImpl iFrameParams = new IFrameParamsImpl(descriptor);
        final String functionConfiguration = StringUtils.defaultString((String) params.get(POST_FUNCTION_CONFIGURATION));
        final Map<String, Object> extraMarkupParams = ImmutableMap.<String, Object>of(
                POST_FUNCTION_CONFIGURATION, functionConfiguration,
                POST_FUNCTION_CONFIGURATION_UUID, uuid,
                "scriptUrl", getDialogScriptUrl()
        );
        // Render the extra markup containing configuration value and descriptor uuid.
        final StringWriter writer = new StringWriter();
        templateRenderer.render(POST_FUNCTION_EXTRA_MARKUP, extraMarkupParams, writer);
        iFrameParams.setParamNoEscape("extraMarkupHtml", writer.toString());
        return iFrameParams;
    }

    private String getDialogScriptUrl()
    {
        ModuleDescriptor<?> dialogModuleDescriptor = pluginRetrievalService.getPlugin().getModuleDescriptor("dialog");
        for (ResourceDescriptor descriptor : dialogModuleDescriptor.getResourceDescriptors())
        {
            String src = webResourceUrlProvider.getStaticPluginResourceUrl(dialogModuleDescriptor, descriptor.getName(), UrlMode.AUTO);
            if (src.endsWith("js"))
            {
                return src;
            }
        }
        return null;
    }

    private URI iFrameURI(final String resourceName)
    {
        try
        {
            if (resourceName.equals(RESOURCE_NAME_VIEW))
            {
                return createURI(descriptor.element("view"));
            }
            else if (resourceName.equals(RESOURCE_NAME_INPUT_PARAMETERS))
            {
                return createURI(descriptor.element("create"));
            }
            else
            {
                return createURI(descriptor.element("edit"));
            }
        }
        catch (URISyntaxException e)
        {
            throw new PluginParseException(e);
        }
    }

    private URI createURI(final Element element) throws URISyntaxException
    {
        return new URI(getRequiredAttribute(element, "url"));
    }

    private Resources createResourceDescriptors(final Map<String, URI> workflowFunctionActionUris)
    {
        final Element root = DocumentFactory.getInstance()
                .createDocument()
                .addElement("resources");
        for (String resourceType : workflowFunctionActionUris.keySet())
        {
            root.addElement("resource")
                    .addAttribute("name", resourceType)
                    .addAttribute("type", "velocity")
                    .addAttribute("location", "location");
        }
        return Resources.fromXml(root);
    }

    @SuppressWarnings("unchecked")
    public Class<WorkflowPluginFunctionFactory> getImplementationClass()
    {
        return (Class) RemoteWorkflowPostFunctionProvider.class;
    }

    @Override
    public WorkflowPluginFunctionFactory getModule()
    {
        return new RemoteWorkflowFunctionPluginFactory();
    }

    @Override
    public boolean isEditable()
    {
        return super.isEditable();
    }
}
