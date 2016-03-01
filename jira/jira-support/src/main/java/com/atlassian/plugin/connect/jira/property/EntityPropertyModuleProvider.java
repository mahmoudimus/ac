package com.atlassian.plugin.connect.jira.property;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyModuleProvider extends AbstractJiraConnectModuleProvider<EntityPropertyModuleBean> {

    private static final EntityPropertyModuleMeta META = new EntityPropertyModuleMeta();

    private final ConnectEntityPropertyModuleDescriptorFactory descriptorFactory;

    @Autowired
    public EntityPropertyModuleProvider(PluginRetrievalService pluginRetrievalService,
                                        ConnectJsonSchemaValidator schemaValidator,
                                        ConnectEntityPropertyModuleDescriptorFactory descriptorFactory) {
        super(pluginRetrievalService, schemaValidator);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public ConnectModuleMeta<EntityPropertyModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ModuleDescriptor<?>> createPluginModuleDescriptors(List<EntityPropertyModuleBean> modules, ConnectAddonBean addon) {
        return Lists.transform(modules, bean -> descriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin()));
    }
}
