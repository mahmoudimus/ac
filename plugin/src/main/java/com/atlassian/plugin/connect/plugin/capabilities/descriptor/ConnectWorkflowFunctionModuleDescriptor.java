package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionProvider;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW;
import static com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION;
import static com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory.POST_FUNCTION_CONFIGURATION_UUID;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A ModuleDescriptor for Connect's version of a Jira Workflow Post Function.
 */
public class ConnectWorkflowFunctionModuleDescriptor extends WorkflowFunctionModuleDescriptor
{
    private static final Logger log = LoggerFactory.getLogger(ConnectWorkflowFunctionModuleDescriptor.class);

    private static final String POST_FUNCTION_EXTRA_MARKUP = "velocity/jira/workflow/post-function-extra-markup.vm";

    private final TypeResolver remoteWorkflowTypeResolver;
    private final IFrameRenderer iFrameRenderer;
    private final TemplateRenderer templateRenderer;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final PluginRetrievalService pluginRetrievalService;
    private final OSWorkflowConfigurator workflowConfigurator;
    private final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry;

    private WorkflowPostFunctionCapabilityBean capabilityBean;

    private ImmutableMap<String, URI> workflowFunctionActionUris;
    private URI triggeredUri;
    private String completeKey;

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
            final DelegatingComponentAccessor componentAccessor)
    {
        super(authenticationContext, componentAccessor.getComponent(OSWorkflowConfigurator.class),
                componentAccessor.getComponent(ComponentClassManager.class), moduleFactory);

        this.webHookConsumerRegistry = checkNotNull(webHookConsumerRegistry);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.webResourceUrlProvider = checkNotNull(webResourceUrlProvider);
        this.pluginRetrievalService = checkNotNull(pluginRetrievalService);
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

    public void init(Plugin plugin, WorkflowPostFunctionCapabilityBean capabilityBean) throws PluginParseException
    {
        this.plugin = plugin;
        this.capabilityBean = capabilityBean;
        this.completeKey = buildCompleteKey(plugin, capabilityBean.getKey());

        final ImmutableMap.Builder<String, URI> workflowFunctionActionUrisMapBuilder = ImmutableMap.builder();
        if (capabilityBean.getView() != null)
        {
            workflowFunctionActionUrisMapBuilder.put(RESOURCE_NAME_VIEW, getResourceIFrameURI(RESOURCE_NAME_VIEW));
        }
        if (capabilityBean.getCreate() != null)
        {
            workflowFunctionActionUrisMapBuilder.put(RESOURCE_NAME_INPUT_PARAMETERS, getResourceIFrameURI(RESOURCE_NAME_INPUT_PARAMETERS));
        }
        if (capabilityBean.getEdit() != null)
        {
            workflowFunctionActionUrisMapBuilder.put(RESOURCE_NAME_EDIT_PARAMETERS, getResourceIFrameURI(RESOURCE_NAME_EDIT_PARAMETERS));
        }
        this.workflowFunctionActionUris = workflowFunctionActionUrisMapBuilder.build();
        this.resources = createResourceDescriptors(workflowFunctionActionUris);
        this.triggeredUri = createUri(capabilityBean.getTriggered());
    }


    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        throw new UnsupportedOperationException("This descriptor does not support elements");
    }

    @Override
    public String getName()
    {
        //TODO: Handle i18n
        return capabilityBean.getName().getValue();
    }

    @Override
    public String getDescription()
    {
        //TODO: Handle i18n
        return capabilityBean.getDescription().getValue();
    }

    @Override
    public String getHtml(String resourceName)
    {
        return getHtml(resourceName, (AbstractDescriptor) null);
    }

    @Override
    public String getHtml(String resourceName, Map<String, ?> startingParams)
    {
        return getHtml(resourceName);
    }

    @Override
    public void writeHtml(String resourceName, Map<String, ?> startingParams, Writer writer) throws IOException
    {
        writer.write(getHtml(resourceName));
    }

    @Override
    public String getHtml(final String resourceName, @Nullable final AbstractDescriptor functionDescriptor)
    {
        final Map<String, ?> params = getModule().getVelocityParams(resourceName, functionDescriptor);
        final String uuid = (String) params.get(POST_FUNCTION_CONFIGURATION_UUID);

        if (isValidUUID(uuid))
        {
            return renderIFrame(params, uuid, resourceName);
        }
        else
        {
            log.error("Invalid UUID: " + uuid);
            return "";
        }
    }

    private String renderIFrame(Map<String, ?> params, String uuid, String resourceName)
    {
        try
        {
            final IFrameParams iFrameParams = createIFrameParams(params, uuid);
            final String namespace = getKey() + uuid;
            return iFrameRenderer.render(
                    new IFrameContextImpl(getPluginKey(),
                            workflowFunctionActionUris.get(resourceName),
                            namespace,
                            iFrameParams),
                    "",
                    ImmutableMap.of(POST_FUNCTION_CONFIGURATION_UUID, new String[]{uuid}),
                    ComponentAccessor.getJiraAuthenticationContext().getUser().getDisplayName(),
                    Collections.<String, Object>emptyMap());
        }
        catch (IOException e)
        {
            log.warn("Could not render iFrame", e);
            return "";
        }
    }

    private static boolean isValidUUID(String uuid)
    {
        if (null == uuid)
        {
            return false;
        }
        try
        {
            UUID.fromString(uuid);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        return true;
    }

    private IFrameParams createIFrameParams(final Map<String, ?> params, final String uuid)
            throws IOException
    {
        final IFrameParamsImpl iFrameParams = new IFrameParamsImpl();
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
        ArrayList<String> scriptUrls = Lists.newArrayList();
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

    private URI getResourceIFrameURI(final String resourceName)
    {
        if (resourceName.equals(RESOURCE_NAME_VIEW))
        {
            return createUri(capabilityBean.getView());
        }
        else if (resourceName.equals(RESOURCE_NAME_INPUT_PARAMETERS))
        {
            return createUri(capabilityBean.getCreate());
        }
        else
        {
            return createUri(capabilityBean.getEdit());
        }
    }

    @Override
    public boolean isOrderable()
    {
        return true; // all remote post-functions are orderable
    }

    @Override
    public boolean isUnique()
    {
        return false; // we currently cannot support unique remote post-functions as they share a common implementation
    }

    @Override
    public boolean isDeletable()
    {
        return true; // all remote post-functions are deletable
    }

    @Override
    public boolean isAddable(String actionType)
    {
        return true; // remote post-functions can be added to any transition
    }

    @Override
    public Integer getWeight()
    {
        return null; // JIRA assumes only system post-functions should have weight
    }

    @Override
    public boolean isDefault()
    {
        return false; // remote post-functions are not added by default
    }

    @Override
    public Class getImplementationClass()
    {
        return RemoteWorkflowPostFunctionProvider.class;
    }

    @Override
    public boolean isEditable()
    {
        return capabilityBean.hasEdit();
    }

    @Override
    protected void assertResourceExists(String type, String name) throws PluginParseException
    {
        // no-op
    }

    @Override
    protected WorkflowPluginFunctionFactory createModule()
    {
        return new RemoteWorkflowFunctionPluginFactory();
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
        // no-op
    }

    @Override
    protected void loadClass(Plugin plugin, Element element) throws PluginParseException
    {
        throw new UnsupportedOperationException("This descriptor doesn't support elements");
    }

    @Override
    public void enabled()
    {
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
    }

    @Override
    public String getCompleteKey()
    {
        return completeKey;
    }

    private URI getTriggeredUri()
    {
        return triggeredUri;
    }

    private URI createUri(UrlBean urlBean) throws PluginParseException
    {
        if (urlBean == null || !urlBean.hasUrl())
        {
            throw new PluginParseException("URL is required");
        }
        try
        {
            return urlBean.createUri();
        }
        catch (URISyntaxException e)
        {
            throw new PluginParseException(e);
        }
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

    @Override
    public boolean isSystemModule()
    {
        return false;
    }

    @Override
    public String getKey()
    {
        return capabilityBean.getKey();
    }

    @Override
    public String getModuleClassName()
    {
        return RemoteWorkflowFunctionPluginFactory.class.getName();
    }

    @Override
    public Map<String, String> getParams()
    {
        return ImmutableMap.of();
    }

    @Override
    public String getI18nNameKey()
    {
        return capabilityBean.getName().getI18n();
    }

    @Override
    public String getDescriptionKey()
    {
        return capabilityBean.getDescription().getI18n();
    }

    //TODO: Copied from super-class
    private String buildCompleteKey(final Plugin plugin, final String moduleKey)
    {
        if (plugin == null)
        {
            return null;
        }

        final StringBuffer completeKeyBuffer = new StringBuffer(32);
        completeKeyBuffer.append(plugin.getKey()).append(":").append(moduleKey);
        return completeKeyBuffer.toString();
    }
}
