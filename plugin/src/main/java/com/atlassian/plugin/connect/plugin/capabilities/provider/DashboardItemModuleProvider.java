package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DashboardItemModuleBeanFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class DashboardItemModuleProvider implements ConnectModuleProvider<DashboardItemModuleBean>
{
    private final DashboardItemModuleBeanFactory dashboardItemModuleBeanFactory;

    @Autowired
    public DashboardItemModuleProvider(final DashboardItemModuleBeanFactory dashboardItemModuleBeanFactory)
    {
        this.dashboardItemModuleBeanFactory = dashboardItemModuleBeanFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext,
            final Plugin plugin,
            final String jsonFieldName,
            final List<DashboardItemModuleBean> beans)
    {
        return Lists.transform(beans, new Function<DashboardItemModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final DashboardItemModuleBean bean)
            {
                return dashboardItemModuleBeanFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
            }
        });
    }
}
