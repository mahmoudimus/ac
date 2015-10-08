package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.ConnectEntityPropertyModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyModuleProvider extends AbstractJiraConnectModuleProvider<EntityPropertyModuleBean>
{

    private static final EntityPropertyModuleMeta META = new EntityPropertyModuleMeta();

    private final ConnectEntityPropertyModuleDescriptorFactory descriptorFactory;

    @Autowired
    public EntityPropertyModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ConnectEntityPropertyModuleDescriptorFactory descriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<EntityPropertyModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        return Lists.transform(modules, new Function<EntityPropertyModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final EntityPropertyModuleBean bean)
            {
                return descriptorFactory.createModuleDescriptor(moduleProviderContext,
                        pluginRetrievalService.getPlugin(), bean);
            }
        });
    }

    @Override
    public ConnectModuleMeta<EntityPropertyModuleBean> getMeta()
    {
        return META;
    }
}
