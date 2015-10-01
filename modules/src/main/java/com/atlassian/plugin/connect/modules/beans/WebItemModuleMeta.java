package com.atlassian.plugin.connect.modules.beans;

public class WebItemModuleMeta implements ConnectModuleMeta<WebItemModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "webItems";
    }

    @Override
    public Class<WebItemModuleBean> getBeanClass()
    {
        return WebItemModuleBean.class;
    }
}
