package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WorkflowPostFunctionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@JiraComponent
public class WorkflowPostFunctionModuleProvider implements ConnectModuleProvider<WorkflowPostFunctionModuleBean>
{
    private final WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory;

    @Autowired
    public WorkflowPostFunctionModuleProvider(WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory)
    {
        this.workflowPostFunctionFactory = workflowPostFunctionFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<WorkflowPostFunctionModuleBean> beans)
    {

        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WorkflowPostFunctionModuleBean bean : beans)
        {
            descriptors.add(beanToDescriptor(plugin, addonBundleContext, bean));
        }

        return descriptors;
    }

    private ModuleDescriptor beanToDescriptor(Plugin plugin, BundleContext addonBundleContext, WorkflowPostFunctionModuleBean bean)
    {
        return workflowPostFunctionFactory.createModuleDescriptor(plugin, addonBundleContext, bean);
    }
}
