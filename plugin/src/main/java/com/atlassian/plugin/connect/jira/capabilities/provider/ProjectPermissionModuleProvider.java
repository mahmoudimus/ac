package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.permission.GlobalPermissionModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.permission.ProjectPermissionModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class ProjectPermissionModuleProvider implements ConnectModuleProvider<ProjectPermissionModuleBean>
{
    private final ProjectPermissionModuleDescriptorFactory descriptorFactory;

    @Autowired
    public ProjectPermissionModuleProvider(ProjectPermissionModuleDescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, final String jsonFieldName, final List<ProjectPermissionModuleBean> beans)
    {
        return Lists.transform(beans, new Function<ProjectPermissionModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final ProjectPermissionModuleBean bean)
            {
                return descriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            }
        });
    }
}
