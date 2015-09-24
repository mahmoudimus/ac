package com.atlassian.plugin.connect;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;

import java.util.List;

public class TestJiraModuleProvider extends AbstractConnectModuleProvider<TestJiraModuleBean> 
{

    @Override
    public String getSchemaPrefix()
    {
        return null;
    }

    @Override
    public ConnectModuleMeta getMeta()
    {
        return new ConnectModuleMeta()
        {
            @Override
            public boolean multipleModulesAllowed()
            {
                return true;
            }

            @Override
            public String getDescriptorKey()
            {
                return "jiraTestModules";
            }

            @Override
            public Class getBeanClass()
            {
                return TestJiraModuleBean.class;
            }
        };
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, List<TestJiraModuleBean> beans)
    {
        return ImmutableList.of((ModuleDescriptor)new TestJiraModuleDescriptor());
    }
}
