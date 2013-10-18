package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.AbstractConnectTabPanelModuleDescriptorFactory;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractConnectTabPanelModuleProvider<B extends AbstractConnectTabPanelCapabilityBean,
        F extends AbstractConnectTabPanelModuleDescriptorFactory> implements ConnectModuleProvider<B>
{
    private final F moduleFactory;

    public AbstractConnectTabPanelModuleProvider(F moduleFactory)
    {
        this.moduleFactory = moduleFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<B> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (B bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, B bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        B newBean = createCapabilityBean(bean);
        descriptors.add(createModuleDescriptor(plugin, addonBundleContext, newBean));

        return descriptors;
    }

    protected abstract B createCapabilityBean(B bean);

    private ModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, B newBean)
    {
        return moduleFactory.createModuleDescriptor(plugin, addonBundleContext, newBean);
    }
}
