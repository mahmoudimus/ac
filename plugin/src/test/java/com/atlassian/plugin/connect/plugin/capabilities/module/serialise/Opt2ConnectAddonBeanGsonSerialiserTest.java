package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.Module;

import static org.junit.Assert.*;

public class Opt2ConnectAddonBeanGsonSerialiserTest extends BaseConnectAddonBeanGsonSerialiserTest
{

    public Opt2ConnectAddonBeanGsonSerialiserTest()
    {
        super(new ModuleListSerialiserOption2());
    }

    @Override
    protected EntityPropertyModuleBean convert(Object bean)
    {
        return (EntityPropertyModuleBean) ((Module)bean).toBean(EntityPropertyModuleBean.class);
    }
}