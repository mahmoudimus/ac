package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.report.ConnectReportModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleMeta;
import com.atlassian.plugin.connect.spi.module.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @since 1.2
 */
@JiraComponent
public class ReportModuleProvider extends AbstractConnectModuleProvider<ReportModuleBean>
{
    private final ConnectReportModuleDescriptorFactory moduleDescriptorFactory;

    @Autowired
    public ReportModuleProvider(final ConnectReportModuleDescriptorFactory moduleDescriptorFactory)
    {
        this.moduleDescriptorFactory = moduleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<ReportModuleBean> beans)
    {
        return Lists.transform(beans, new Function<ReportModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final ReportModuleBean bean)
            {
                return moduleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            }
        });
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }

    @Override
    public ConnectModuleMeta<ReportModuleBean> getMeta()
    {
        return new ReportModuleMeta();
    }
}
