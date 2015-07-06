package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.ConnectEntityPropertyModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyModuleProvider implements ConnectModuleProvider
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
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<JsonObject> modules)
    {
        return Lists.transform(modules, new Function<JsonObject, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final JsonObject module)
            {
                EntityPropertyModuleBean bean = new Gson().fromJson(module, EntityPropertyModuleBean.class);

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
}
