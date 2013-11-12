package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY;
import static com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWorkflowFunctionModuleDescriptor.TRIGGERED_URL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Factory that creates WorkflowFunctionModuleDescriptors from WorkflowPostFunctionCapabilityBeans
 */
@JiraComponent
public class WorkflowPostFunctionModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WorkflowPostFunctionCapabilityBean, WorkflowFunctionModuleDescriptor>
{
    private final ConnectAutowireUtil connectAutowireUtil;

    @Autowired
    public WorkflowPostFunctionModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        this.connectAutowireUtil = checkNotNull(connectAutowireUtil);
    }

    @Override
    public WorkflowFunctionModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WorkflowPostFunctionCapabilityBean bean)
    {
        Element element = createDOMElement(bean);
        ConnectWorkflowFunctionModuleDescriptor moduleDescriptor = connectAutowireUtil.createBean(ConnectWorkflowFunctionModuleDescriptor.class);
        moduleDescriptor.init(plugin, element);
        return moduleDescriptor;
    }

    private Element createDOMElement(WorkflowPostFunctionCapabilityBean bean)
    {
        Element element = new DOMElement("remote-workflow-post-function");

        element.addAttribute("class", RemoteWorkflowFunctionPluginFactory.class.getName());
        element.addElement("function-class").addText(RemoteWorkflowPostFunctionProvider.class.getName());

        element.addAttribute("key", bean.getKey());
        element.addAttribute("name", bean.getName().getValue());
        element.addAttribute("i18n-name-key", bean.getName().getI18n());
        element.addElement("description")
                .addText(bean.getDescription().getValue())
                .addAttribute("key", bean.getDescription().getI18n());

        element.addAttribute(TRIGGERED_URL, bean.getTriggered().getUrl());

        if (bean.hasView())
        {
            addResource(element, RESOURCE_NAME_VIEW, bean.getView().getUrl());
        }
        if (bean.hasEdit())
        {
            addResource(element, RESOURCE_NAME_EDIT_PARAMETERS, bean.getEdit().getUrl());
        }
        if (bean.hasCreate())
        {
            addResource(element, RESOURCE_NAME_INPUT_PARAMETERS, bean.getCreate().getUrl());
        }
        return element;
    }

    private void addResource(Element element, String resourceName, String url)
    {
        element.addElement("resource")
                .addAttribute("name", resourceName)
                .addAttribute("type", RESOURCE_TYPE_VELOCITY)
                .addAttribute("location", url);
    }
}
