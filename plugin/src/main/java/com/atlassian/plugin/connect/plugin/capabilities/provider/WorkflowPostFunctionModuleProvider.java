package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WorkflowPostFunctionModuleDescriptorFactory;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WorkflowPostFunctionModuleProvider implements ConnectModuleProvider<WorkflowPostFunctionCapabilityBean>
{

    private final WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory;

    @Autowired
    public WorkflowPostFunctionModuleProvider(WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        this.workflowPostFunctionFactory = workflowPostFunctionFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<WorkflowPostFunctionCapabilityBean> beans)
    {

        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WorkflowPostFunctionCapabilityBean bean : beans)
        {
            descriptors.add(beanToDescriptor(plugin, addonBundleContext, bean));
        }

        return descriptors;
    }

    private ModuleDescriptor beanToDescriptor(Plugin plugin, BundleContext addonBundleContext, WorkflowPostFunctionCapabilityBean bean)
    {
        return workflowPostFunctionFactory.createModuleDescriptor(plugin, addonBundleContext, bean);
    }

}
