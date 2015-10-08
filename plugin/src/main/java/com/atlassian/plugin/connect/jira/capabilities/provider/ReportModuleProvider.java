package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.report.ConnectReportModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @since 1.2
 */
@JiraComponent
public class ReportModuleProvider extends AbstractJiraConnectModuleProvider<ReportModuleBean>
{

    private static final ReportModuleMeta META = new ReportModuleMeta();

    private final ConnectReportModuleDescriptorFactory moduleDescriptorFactory;

    @Autowired
    public ReportModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ConnectReportModuleDescriptorFactory moduleDescriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.moduleDescriptorFactory = moduleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ReportModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        return Lists.transform(modules, new Function<ReportModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final ReportModuleBean bean)
            {
                return moduleDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                        pluginRetrievalService.getPlugin(), bean);
            }
        });
    }

    @Override
    public ConnectModuleMeta<ReportModuleBean> getMeta()
    {
        return META;
    }
}
