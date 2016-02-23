package com.atlassian.plugin.connect.jira.permission;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class GlobalPermissionModuleProvider extends AbstractJiraConnectModuleProvider<GlobalPermissionModuleBean> {

    private static final GlobalPermissionModuleMeta META = new GlobalPermissionModuleMeta();

    private final GlobalPermissionModuleDescriptorFactory descriptorFactory;

    @Autowired
    public GlobalPermissionModuleProvider(PluginRetrievalService pluginRetrievalService,
                                          ConnectJsonSchemaValidator schemaValidator,
                                          GlobalPermissionModuleDescriptorFactory descriptorFactory) {
        super(pluginRetrievalService, schemaValidator);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public ConnectModuleMeta<GlobalPermissionModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(final List<GlobalPermissionModuleBean> modules, ConnectAddonBean addon) {
        return Lists.transform(modules, bean -> descriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin()));
    }
}
