package com.atlassian.plugin.connect.modules.beans;

public class DashboardItemModuleMeta implements ConnectModuleMeta<DashboardItemModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraDashboardItems";
    }

    @Override
    public Class<DashboardItemModuleBean> getBeanClass()
    {
        return DashboardItemModuleBean.class;
    }
}
