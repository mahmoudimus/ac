package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.guava.collect.Lists;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectVersionWarningCategoryModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionWarningCategoryModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Module Provider for a {@link com.atlassian.jira.plugin.devstatus.api.VersionWarningCategory} module.
 */
@JiraComponent
public class ConnectVersionWarningCategoryModuleProvider
        implements ConnectModuleProvider<ConnectVersionWarningCategoryModuleBean>
{
    private final ConnectVersionWarningCategoryModuleDescriptorFactory connectVersionWarningCategoryModuleDescriptorFactory;

    @Autowired
    public ConnectVersionWarningCategoryModuleProvider(
            ConnectVersionWarningCategoryModuleDescriptorFactory connectVersionWarningCategoryModuleDescriptorFactory)
    {
        this.connectVersionWarningCategoryModuleDescriptorFactory = connectVersionWarningCategoryModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(
            ConnectModuleProviderContext moduleProviderContext, 
            Plugin theConnectPlugin, 
            String jsonFieldName, 
            List<ConnectVersionWarningCategoryModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectVersionWarningCategoryModuleBean bean : beans)
        {
            builder.addAll(beanToDescriptors(moduleProviderContext, theConnectPlugin, bean));
        }

        return builder.build();
    }
    
    private Collection<? extends ModuleDescriptor> beanToDescriptors(
            ConnectModuleProviderContext moduleProviderContext,
            Plugin theConnectPlugin,
            ConnectVersionWarningCategoryModuleBean bean)
    {
        List<ModuleDescriptor> moduleDescriptors = Lists.newArrayList();
        moduleDescriptors.add(connectVersionWarningCategoryModuleDescriptorFactory.createModuleDescriptor(
                moduleProviderContext,
                theConnectPlugin,
                bean));
        return moduleDescriptors;
    }
}
