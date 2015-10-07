package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.ConnectEntityPropertyModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleMeta;
import com.atlassian.plugin.connect.spi.module.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyModuleProvider extends AbstractConnectModuleProvider<EntityPropertyModuleBean>
{

    private static final EntityPropertyModuleMeta META = new EntityPropertyModuleMeta();

    private final ConnectEntityPropertyModuleDescriptorFactory descriptorFactory;

    @Autowired
    public EntityPropertyModuleProvider(ConnectEntityPropertyModuleDescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<EntityPropertyModuleBean> beans)
    {
        return Lists.transform(beans, new Function<EntityPropertyModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final EntityPropertyModuleBean bean)
            {
                return descriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            }
        });
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }

    @Override
    public ConnectModuleMeta<EntityPropertyModuleBean> getMeta()
    {
        return META;
    }
}
