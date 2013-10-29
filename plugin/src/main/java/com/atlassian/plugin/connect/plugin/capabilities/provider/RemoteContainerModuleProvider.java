package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RemoteContainerModuleDescriptorFactory;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoteContainerModuleProvider implements ConnectModuleProvider<RemoteContainerCapabilityBean>
{
    private final RemoteContainerModuleDescriptorFactory containerModuleDescriptorFactory;

    @Autowired
    public RemoteContainerModuleProvider(RemoteContainerModuleDescriptorFactory containerModuleDescriptorFactory)
    {
        this.containerModuleDescriptorFactory = containerModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<RemoteContainerCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        //we can only have one so we'll just take the first one.
        if(!beans.isEmpty())
        {
            descriptors.addAll(beanToDescriptors(plugin, addonBundleContext, beans.get(0)));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, RemoteContainerCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        descriptors.add(containerModuleDescriptorFactory.createModuleDescriptor(plugin,addonBundleContext,bean));
        

        return descriptors;
    }
}
