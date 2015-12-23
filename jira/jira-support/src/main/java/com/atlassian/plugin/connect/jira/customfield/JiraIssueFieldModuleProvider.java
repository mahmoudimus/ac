package com.atlassian.plugin.connect.jira.customfield;

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

    private final CustomFieldTypeDescriptorFactory customFieldTypeDescriptorFactory;
    private final CustomFieldSearcherDescriptorFactory customFieldSearcherDescriptorFactory;

    @Autowired
    public JiraIssueFieldModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator, CustomFieldTypeDescriptorFactory customFieldTypeDescriptorFactory, final CustomFieldSearcherDescriptorFactory customFieldSearcherDescriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.customFieldTypeDescriptorFactory = customFieldTypeDescriptorFactory;
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
                        createCustomFieldTypeDescriptor(bean, addon),
                        createCustomFieldSearcherDescriptor(bean, addon)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private ModuleDescriptor createCustomFieldTypeDescriptor(IssueFieldModuleBean bean, ConnectAddonBean addon)
    {
        return customFieldTypeDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
    }

    private ModuleDescriptor createCustomFieldSearcherDescriptor(IssueFieldModuleBean bean, ConnectAddonBean addon)
    {
        return customFieldSearcherDescriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
    }
}
