package com.atlassian.plugin.connect.modules.beans;

public class ProfilePageModuleMeta implements ConnectModuleMeta<ConnectPageModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "profilePages";
    }

    @Override
    public Class<ConnectPageModuleBean> getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
