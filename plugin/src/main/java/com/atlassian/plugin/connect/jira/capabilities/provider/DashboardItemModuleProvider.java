package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.dashboard.DashboardItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleMeta;
import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class DashboardItemModuleProvider extends AbstractConnectModuleProvider<DashboardItemModuleBean>
{
    private final DashboardItemModuleDescriptorFactory dashboardItemModuleDescriptorFactory;

    @Autowired
    public DashboardItemModuleProvider(final DashboardItemModuleDescriptorFactory dashboardItemModuleDescriptorFactory)
    {
        this.dashboardItemModuleDescriptorFactory = dashboardItemModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<DashboardItemModuleBean> beans)
    {
        return Lists.transform(beans, new Function<DashboardItemModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final DashboardItemModuleBean bean)
            {
                return dashboardItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            }
        });
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }

    @Override
    public ConnectModuleMeta getMeta()
    {
        return new DashboardItemModuleMeta();
    }
}
