package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectEntityPropertyModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyModuleProvider implements ConnectModuleProvider<EntityPropertyModuleBean>
{
    private final ConnectEntityPropertyModuleDescriptorFactory descriptorFactory;

    @Autowired
    public EntityPropertyModuleProvider(ConnectEntityPropertyModuleDescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, final String jsonFieldName, final List<EntityPropertyModuleBean> beans)
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

}
