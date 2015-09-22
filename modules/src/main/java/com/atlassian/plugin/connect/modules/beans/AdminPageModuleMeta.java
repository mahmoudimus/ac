package com.atlassian.plugin.connect.modules.beans;

public class AdminPageModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "adminPages";
    }

    @Override
    public Class getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
