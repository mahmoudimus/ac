package com.atlassian.plugin.connect.modules.beans;

public class PostInstallPageModuleMeta implements ConnectModuleMeta<ConnectPageModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return false;
    }

    @Override
    public String getDescriptorKey()
    {
        return "postInstallPage";
    }

    @Override
    public Class<ConnectPageModuleBean> getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
