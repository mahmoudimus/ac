package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowPostFunctionModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WorkflowPostFunctionCapabilityBean, WorkflowFunctionModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(WorkflowPostFunctionModuleDescriptorFactory.class);

    private final JiraAuthenticationContext authenticationContext;
    private final ModuleFactory moduleFactory;
    private final IFrameRenderer iFrameRenderer;
    private final JiraRestBeanMarshaler jiraRestBeanMarshaler;
    private final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry;
    private final EventPublisher eventPublisher;
    private final TemplateRenderer templateRenderer;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final PluginRetrievalService pluginRetrievalService;


    @Autowired
    public WorkflowPostFunctionModuleDescriptorFactory(final JiraAuthenticationContext authenticationContext,
                                                       final ModuleFactory moduleFactory,
                                                       final IFrameRenderer iFrameRenderer,
                                                       final JiraRestBeanMarshaler jiraRestBeanMarshaler,
                                                       final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry,
                                                       final EventPublisher eventPublisher,
                                                       final TemplateRenderer templateRenderer,
                                                       final WebResourceUrlProvider webResourceUrlProvider,
                                                       final PluginRetrievalService pluginRetrievalService)
    {

        this.authenticationContext = authenticationContext;
        this.moduleFactory = moduleFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.jiraRestBeanMarshaler = jiraRestBeanMarshaler;
        this.webHookConsumerRegistry = webHookConsumerRegistry;
        this.eventPublisher = eventPublisher;
        this.templateRenderer = templateRenderer;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public WorkflowFunctionModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WorkflowPostFunctionCapabilityBean bean)
    {
        ConnectWorkflowFunctionModuleDescriptor moduleDescriptor =  new ConnectWorkflowFunctionModuleDescriptor(
                authenticationContext, moduleFactory, iFrameRenderer, jiraRestBeanMarshaler, webHookConsumerRegistry,
                eventPublisher, templateRenderer, webResourceUrlProvider, pluginRetrievalService);

        moduleDescriptor.init(plugin, bean);

        return moduleDescriptor;
    }

}
