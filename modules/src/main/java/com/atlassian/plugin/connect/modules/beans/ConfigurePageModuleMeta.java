package com.atlassian.plugin.connect.modules.beans;

public class ConfigurePageModuleMeta implements ConnectModuleMeta
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
    public Class getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
