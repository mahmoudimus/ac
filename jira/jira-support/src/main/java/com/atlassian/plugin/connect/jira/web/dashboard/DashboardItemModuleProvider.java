package com.atlassian.plugin.connect.jira.web.dashboard;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class DashboardItemModuleProvider extends AbstractJiraConnectModuleProvider<DashboardItemModuleBean>
{

    private static final DashboardItemModuleMeta META = new DashboardItemModuleMeta();

    private final DashboardItemModuleDescriptorFactory dashboardItemModuleDescriptorFactory;
    private final ConditionLoadingValidator conditionLoadingValidator;

    @Autowired
    public DashboardItemModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            DashboardItemModuleDescriptorFactory dashboardItemModuleDescriptorFactory,
            ConditionLoadingValidator conditionLoadingValidator)
    {
        super(pluginRetrievalService, schemaValidator);
        this.dashboardItemModuleDescriptorFactory = dashboardItemModuleDescriptorFactory;
        this.conditionLoadingValidator = conditionLoadingValidator;
    }

    @Override
    public ConnectModuleMeta<DashboardItemModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<DashboardItemModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<DashboardItemModuleBean> dashboardItems = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), dashboardItems);
        return dashboardItems;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<DashboardItemModuleBean> modules, ConnectAddonBean addon)
    {
        return Lists.transform(modules, bean -> dashboardItemModuleDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin()));
    }
}
