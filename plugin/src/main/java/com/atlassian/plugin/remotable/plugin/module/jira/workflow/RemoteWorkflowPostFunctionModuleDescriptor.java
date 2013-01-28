package com.atlassian.plugin.remotable.plugin.module.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.remotable.spi.module.IFrameRenderer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ConsumerKey;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookConsumerRegistry;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.annotation.Nullable;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW;
import static com.atlassian.plugin.remotable.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION;
import static com.atlassian.plugin.remotable.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION_UUID;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getRequiredAttribute;

/**
 * A remote post-function module descriptor.
 */
public class RemoteWorkflowPostFunctionModuleDescriptor extends WorkflowFunctionModuleDescriptor
{
    private static final String POST_FUNCTION_EXTRA_MARKUP = "velocity/jira/workflow/post-function-extra-markup.vm";

    private final ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry;
    private final OSWorkflowConfigurator workflowConfigurator;
    private final IFrameRenderer iFrameRenderer;
    private final TypeResolver remoteWorkflowTypeResolver;
    private final TemplateRenderer templateRenderer;

    private Element descriptor;
    private Map<String, URI> workflowFunctionActionUris;
    private String moduleKey;
    private URI publishURI;

    public RemoteWorkflowPostFunctionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory,
            final IFrameRenderer iFrameRenderer,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler,
            final ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry,
            final EventPublisher eventPublisher,
            final TemplateRenderer templateRenderer)
    {
        super(authenticationContext,
                ComponentAccessor.getComponent(OSWorkflowConfigurator.class),
                ComponentAccessor.getComponent(ComponentClassManager.class),
                moduleFactory);

        this.webHookConsumerRegistry = webHookConsumerRegistry;
        this.templateRenderer = templateRenderer;
        this.workflowConfigurator = ComponentAccessor.getComponent(OSWorkflowConfigurator.class);
        this.iFrameRenderer = iFrameRenderer;

        this.remoteWorkflowTypeResolver = new TypeResolver()
        {
            @Override
            public FunctionProvider getFunction(final String type, final Map args) throws WorkflowException
            {
                return new RemoteWorkflowPostFunctionProvider(eventPublisher, jiraRestBeanMarshaler, new ConsumerKey(plugin.getKey(), moduleKey));
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
                new ConsumerKey(plugin.getKey(), moduleKey),
                publishURI);
    }

    @Override
    public void disabled()
    {
        workflowConfigurator.unregisterTypeResolver(RemoteWorkflowPostFunctionProvider.class.getName(), remoteWorkflowTypeResolver);
        this.webHookConsumerRegistry.unregister(
                RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID,
                new ConsumerKey(plugin.getKey(), moduleKey),
                publishURI);
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
            final User loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            final Map<String, ?> params = getModule().getVelocityParams(resourceName, functionDescriptor);
            final String uuid = (String) params.get(POST_FUNCTION_CONFIGURATION_UUID);
            final IFrameParams iFrameParams = createIFrameParams(params, uuid);
            final String namespace = moduleKey + uuid;
            return iFrameRenderer.render(
                    new IFrameContextImpl(getPluginKey(),
                            workflowFunctionActionUris.get(resourceName),
                            namespace,
                            iFrameParams),
                    "",
                    ImmutableMap.of(POST_FUNCTION_CONFIGURATION_UUID, new String[] { uuid }),
                    loggedInUser.getDisplayName());
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
                POST_FUNCTION_CONFIGURATION_UUID, uuid
        );
        // Render the extra markup containing configuration value and descriptor uuid.
        final StringWriter writer = new StringWriter();
        templateRenderer.render(POST_FUNCTION_EXTRA_MARKUP, extraMarkupParams, writer);
        iFrameParams.setParamNoEscape("extraMarkupHtml", writer.toString());
        return iFrameParams;
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
