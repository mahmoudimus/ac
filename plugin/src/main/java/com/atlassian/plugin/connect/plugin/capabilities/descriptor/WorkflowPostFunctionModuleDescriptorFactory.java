package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionModuleDescriptor;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

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
        Element workflowElement = new DOMElement("remote-workflow-post-function");

        workflowElement.addAttribute("key", escapeHtml(bean.getKey()));
        workflowElement.addAttribute("name", escapeHtml(bean.getName().getValue()));
        workflowElement.addAttribute("i18n-name-key", bean.getName().getI18n());
        workflowElement.addAttribute("url", bean.getTriggered().getUrl());
        workflowElement.addAttribute("orderable", "true");
        workflowElement.addAttribute("deletable", "true");
        workflowElement.addAttribute("unique", Boolean.toString(!bean.allowMultiple()));

        workflowElement.addElement("view").addAttribute("url", bean.getView().getUrl());
        workflowElement.addElement("edit").addAttribute("url", bean.getView().getUrl());
        workflowElement.addElement("create").addAttribute("url", bean.getView().getUrl());


        RemoteWorkflowPostFunctionModuleDescriptor moduleDescriptor =  new RemoteWorkflowPostFunctionModuleDescriptor(
                authenticationContext, moduleFactory, iFrameRenderer, jiraRestBeanMarshaler, webHookConsumerRegistry,
                eventPublisher, templateRenderer, webResourceUrlProvider, pluginRetrievalService);

        moduleDescriptor.init(plugin, workflowElement);

        if (log.isDebugEnabled())
        {
            log.debug("Created workflow function module item: " + printNode(workflowElement));
        }

        return moduleDescriptor;
    }

}
