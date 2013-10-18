package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.AbstractConnectTabPanelModuleDescriptorFactory;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;

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
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (B bean : beans)
        {
            builder.add(moduleFactory.createModuleDescriptor(plugin, addonBundleContext, createCapabilityBean(bean)));
        }

        return builder.build();
    }

    protected abstract B createCapabilityBean(B bean);

}
