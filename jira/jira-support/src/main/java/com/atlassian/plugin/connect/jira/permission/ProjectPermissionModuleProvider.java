package com.atlassian.plugin.connect.jira.permission;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class ProjectPermissionModuleProvider extends AbstractJiraConnectModuleProvider<ProjectPermissionModuleBean> {

    private static final ProjectPermissionModuleMeta META = new ProjectPermissionModuleMeta();

    private final ConditionLoadingValidator conditionLoadingValidator;
    private final ProjectPermissionModuleDescriptorFactory descriptorFactory;

    @Autowired
    public ProjectPermissionModuleProvider(PluginRetrievalService pluginRetrievalService,
                                           ConnectJsonSchemaValidator schemaValidator,
                                           ConditionLoadingValidator conditionLoadingValidator,
                                           ProjectPermissionModuleDescriptorFactory descriptorFactory) {
        super(pluginRetrievalService, schemaValidator);
        this.conditionLoadingValidator = conditionLoadingValidator;
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public ConnectModuleMeta<ProjectPermissionModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ProjectPermissionModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        List<ProjectPermissionModuleBean> projectPermissions = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), projectPermissions);
        return projectPermissions;
    }

    @Override
    public List<ModuleDescriptor<?>> createPluginModuleDescriptors(List<ProjectPermissionModuleBean> modules, ConnectAddonBean addon) {
        return Lists.transform(modules, bean -> descriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin()));
    }
}
