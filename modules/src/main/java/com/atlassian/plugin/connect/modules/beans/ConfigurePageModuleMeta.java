package com.atlassian.plugin.connect.modules.beans;

public class ConfigurePageModuleMeta implements ConnectModuleMeta<ConnectPageModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return false;
    }

    @Override
    public String getDescriptorKey()
    {
        return "configurePage";
    }

    @Override
    public Class<ConnectPageModuleBean> getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
