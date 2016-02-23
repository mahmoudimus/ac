package com.atlassian.plugin.connect.jira.report;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

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
    public ConnectModuleMeta<ReportModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ReportModuleBean> modules, ConnectAddonBean addon)
    {
        return Lists.transform(modules, bean -> moduleDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin()
        ));
    }
}
