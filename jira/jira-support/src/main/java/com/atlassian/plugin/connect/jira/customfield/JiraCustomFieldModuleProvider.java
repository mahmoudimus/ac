package com.atlassian.plugin.connect.jira.customfield;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class JiraCustomFieldModuleProvider extends AbstractJiraConnectModuleProvider<CustomFieldTypeModuleBean>
{
    private static final CustomFieldTypeModuleMeta META = new CustomFieldTypeModuleMeta();

    private final CustomFieldTypeDescriptorFactory descriptorFactory;

    @Autowired
    public JiraCustomFieldModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator, CustomFieldTypeDescriptorFactory descriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public ConnectModuleMeta<CustomFieldTypeModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(final List<CustomFieldTypeModuleBean> modules, final ConnectAddonBean addon)
    {
        return Lists.transform(modules, new Function<CustomFieldTypeModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final CustomFieldTypeModuleBean bean)
            {
                return descriptorFactory.createModuleDescriptor(bean, addon, pluginRetrievalService.getPlugin());
            }
        });
    }
}
