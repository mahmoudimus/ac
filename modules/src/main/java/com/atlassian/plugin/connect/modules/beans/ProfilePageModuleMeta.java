package com.atlassian.plugin.connect.modules.beans;

public class ProfilePageModuleMeta implements ConnectModuleMeta
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
    public Class getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
