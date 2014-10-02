package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.blah.ConnectModuleProviderRegistry;
import com.atlassian.plugin.connect.plugin.capabilities.provider.blah.ConnectModuleProviderRegistryImpl;
import com.atlassian.plugin.connect.plugin.capabilities.provider.blah.EntityPropertiesModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.Module;

public class Opt1ConnectAddonBeanGsonSerialiserTest extends BaseConnectAddonBeanGsonSerialiserTest
{
    final static ConnectModuleProviderRegistry registry = new ConnectModuleProviderRegistryImpl();
    static {
        registry.register("jiraEntityProperties", new EntityPropertiesModuleProvider());
    }

    public Opt1ConnectAddonBeanGsonSerialiserTest()
    {
        super(new ModuleListSerialiserOption1(registry));
    }

    @Override
    protected EntityPropertyModuleBean convert(Object bean)
    {
        return (EntityPropertyModuleBean) bean;
    }
}