package com.atlassian.plugin.connect.modules.beans;

public class AdminPageModuleMeta implements ConnectModuleMeta<ConnectPageModuleBean>
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
    public Class<ConnectPageModuleBean> getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
