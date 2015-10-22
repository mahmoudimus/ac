package com.atlassian.plugin.connect.jira.permission;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class ProjectPermissionModuleProvider extends AbstractJiraConnectModuleProvider<ProjectPermissionModuleBean>
{

    private static final ProjectPermissionModuleMeta META = new ProjectPermissionModuleMeta();

    private final ProjectPermissionModuleDescriptorFactory descriptorFactory;

    @Autowired
    public ProjectPermissionModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ProjectPermissionModuleDescriptorFactory descriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public ConnectModuleMeta<ProjectPermissionModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ProjectPermissionModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        return Lists.transform(modules, new Function<ProjectPermissionModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final ProjectPermissionModuleBean bean)
            {
                return descriptorFactory.createModuleDescriptor(moduleProviderContext, pluginRetrievalService.getPlugin(), bean);
            }
        });
    }
}
