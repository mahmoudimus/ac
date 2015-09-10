package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.ConnectEntityPropertyModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyModuleProvider extends AbstractConnectModuleProvider<EntityPropertyModuleBean>
{
    public static final String DESCRIPTOR_KEY = "jiraEntityProperties";
    public static final Class BEAN_CLASS = EntityPropertyModuleBean.class;
    
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
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }
}
