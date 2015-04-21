package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.ConnectReportModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @since 1.2
 */
@JiraComponent
public class ReportModuleProvider implements ConnectModuleProvider<ReportModuleBean>
{
    private final ConnectReportModuleDescriptorFactory moduleDescriptorFactory;

    @Autowired
    public ReportModuleProvider(final ConnectReportModuleDescriptorFactory moduleDescriptorFactory)
    {
        this.moduleDescriptorFactory = moduleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin connectPlugin,
            final String jsonFieldName, final List<ReportModuleBean> beans)
    {
        return Lists.transform(beans, new Function<ReportModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final ReportModuleBean reportModuleBean)
            {
                return moduleDescriptorFactory.createModuleDescriptor(moduleProviderContext, connectPlugin, reportModuleBean);
            }
        });
    }
}
