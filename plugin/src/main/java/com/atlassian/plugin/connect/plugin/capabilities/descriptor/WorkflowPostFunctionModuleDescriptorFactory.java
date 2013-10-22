package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Factory that creates WorkflowFunctionModuleDescriptors from WorkflowPostFunctionCapabilityBeans
 */
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
        ConnectWorkflowFunctionModuleDescriptor moduleDescriptor = connectAutowireUtil.createBean(ConnectWorkflowFunctionModuleDescriptor.class);
        moduleDescriptor.init(plugin, bean);
        return moduleDescriptor;
    }

}
