package com.atlassian.plugin.connect.modules.beans;

public class GeneralPageModuleMeta implements ConnectModuleMeta<ConnectPageModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "generalPages";
    }

    @Override
    public Class<ConnectPageModuleBean> getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
