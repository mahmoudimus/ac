package com.atlassian.plugin.connect.jira.field;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.IssueFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.IssueFieldModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JiraComponent
public class JiraIssueFieldModuleProvider extends AbstractJiraConnectModuleProvider<IssueFieldModuleBean>
{
    private static final IssueFieldModuleMeta META = new IssueFieldModuleMeta();

    private final RemoteIssueFieldDescriptorFactory remoteIssueFieldDescriptorFactory;
    private final CustomFieldSearcherDescriptorFactory customFieldSearcherDescriptorFactory;

    @Autowired
    public JiraIssueFieldModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator, RemoteIssueFieldDescriptorFactory remoteIssueFieldDescriptorFactory, final CustomFieldSearcherDescriptorFactory customFieldSearcherDescriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.remoteIssueFieldDescriptorFactory = remoteIssueFieldDescriptorFactory;
        this.customFieldSearcherDescriptorFactory = customFieldSearcherDescriptorFactory;
    }

    @Override
    public ConnectModuleMeta<IssueFieldModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(final List<IssueFieldModuleBean> modules, final ConnectAddonBean addon)
    {
        return modules.stream()
                .map((bean) -> Lists.newArrayList(
                        createCustomFieldSearcherDescriptor(bean, addon), //searcher needs to be created first
                        createIssueFieldDescriptor(bean, addon)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private ModuleDescriptor createIssueFieldDescriptor(IssueFieldModuleBean bean, ConnectAddonBean addon)
    {
        return remoteIssueFieldDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
    }

    private ModuleDescriptor createCustomFieldSearcherDescriptor(IssueFieldModuleBean bean, ConnectAddonBean addon)
    {
        return customFieldSearcherDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
    }
}
