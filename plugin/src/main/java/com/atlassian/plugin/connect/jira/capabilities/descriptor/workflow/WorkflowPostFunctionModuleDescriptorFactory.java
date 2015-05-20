package com.atlassian.plugin.connect.jira.capabilities.descriptor.workflow;

import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.jira.capabilities.provider.WorkflowPostFunctionResource;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.jira.workflow.RemoteWorkflowFunctionPluginFactory;
import com.atlassian.plugin.connect.jira.workflow.RemoteWorkflowPostFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.*;
import static com.atlassian.plugin.connect.jira.capabilities.descriptor.workflow.ConnectWorkflowFunctionModuleDescriptor.TRIGGERED_URL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Factory that creates WorkflowFunctionModuleDescriptors from {@link WorkflowPostFunctionModuleBean}
 */
@JiraComponent
public class WorkflowPostFunctionModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WorkflowPostFunctionModuleBean, WorkflowFunctionModuleDescriptor>
{
    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public WorkflowPostFunctionModuleDescriptorFactory(ConnectContainerUtil connectContainerUtil)
    {
        this.connectContainerUtil = checkNotNull(connectContainerUtil);
    }

    @Override
    public WorkflowFunctionModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext,
                                                                   Plugin theConnectPlugin, WorkflowPostFunctionModuleBean bean)
    {
        Element element = createDOMElement(bean, moduleProviderContext.getConnectAddonBean());
        ConnectWorkflowFunctionModuleDescriptor moduleDescriptor =
                connectContainerUtil.createBean(ConnectWorkflowFunctionModuleDescriptor.class);
        
        moduleDescriptor.init(theConnectPlugin, element);
        return moduleDescriptor;
    }

    private Element createDOMElement(WorkflowPostFunctionModuleBean bean, ConnectAddonBean addon)
    {
        Element element = new DOMElement("remote-workflow-post-function");

        element.addAttribute("class", RemoteWorkflowFunctionPluginFactory.class.getName());
        element.addElement("function-class").addText(RemoteWorkflowPostFunctionProvider.class.getName());

        element.addAttribute("key", bean.getKey(addon));
        element.addAttribute("name", bean.getName().getValue());
        element.addAttribute("i18n-name-key", bean.getName().getI18n());
        element.addElement("description")
                .addText(bean.getDescription().getValue())
                .addAttribute("key", bean.getDescription().getI18n());

        element.addAttribute(TRIGGERED_URL, bean.getTriggered().getUrl());

        if (bean.hasView())
        {
            addResource(element, WorkflowPostFunctionResource.VIEW, bean.getView().getUrl());
        }
        if (bean.hasEdit())
        {
            addResource(element, WorkflowPostFunctionResource.EDIT, bean.getEdit().getUrl());
        }
        if (bean.hasCreate())
        {
            addResource(element, WorkflowPostFunctionResource.CREATE, bean.getCreate().getUrl());
        }
        return element;
    }

    private void addResource(Element element, WorkflowPostFunctionResource resource, String url)
    {
        element.addElement("resource")
                .addAttribute("name", resource.getResource())
                .addAttribute("type", RESOURCE_TYPE_VELOCITY)
                .addAttribute("location", url);
    }
}
