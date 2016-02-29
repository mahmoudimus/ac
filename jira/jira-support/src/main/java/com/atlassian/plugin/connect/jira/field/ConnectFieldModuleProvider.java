package com.atlassian.plugin.connect.jira.field;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JiraComponent
public class ConnectFieldModuleProvider extends AbstractJiraConnectModuleProvider<ConnectFieldModuleBean> {
    private static final ConnectFieldModuleMeta META = new ConnectFieldModuleMeta();

    private final ConnectFieldModuleDescriptorFactory connectFieldModuleDescriptorFactory;
    private final CustomFieldSearcherModuleDescriptorFactory customFieldSearcherModuleDescriptorFactory;

    @Autowired
    public ConnectFieldModuleProvider(PluginRetrievalService pluginRetrievalService,
                                      ConnectJsonSchemaValidator schemaValidator, ConnectFieldModuleDescriptorFactory connectFieldModuleDescriptorFactory, final CustomFieldSearcherModuleDescriptorFactory customFieldSearcherModuleDescriptorFactory) {
        super(pluginRetrievalService, schemaValidator);
        this.connectFieldModuleDescriptorFactory = connectFieldModuleDescriptorFactory;
        this.customFieldSearcherModuleDescriptorFactory = customFieldSearcherModuleDescriptorFactory;
    }

    @Override
    public ConnectModuleMeta<ConnectFieldModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ModuleDescriptor<?>> createPluginModuleDescriptors(final List<ConnectFieldModuleBean> modules, final ConnectAddonBean addon) {
        return modules.stream()
                .map((bean) -> Lists.<ModuleDescriptor<?>>newArrayList(
                        createSearcherDescriptor(bean, addon), //searcher needs to be created first
                        createConnectFieldDescriptor(bean, addon)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private ModuleDescriptor<CustomFieldType> createConnectFieldDescriptor(ConnectFieldModuleBean bean, ConnectAddonBean addon) {
        return connectFieldModuleDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
    }

    private ModuleDescriptor<CustomFieldSearcher> createSearcherDescriptor(ConnectFieldModuleBean bean, ConnectAddonBean addon) {
        return customFieldSearcherModuleDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
    }
}
