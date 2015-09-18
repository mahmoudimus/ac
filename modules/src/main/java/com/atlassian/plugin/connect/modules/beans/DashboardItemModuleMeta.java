package com.atlassian.plugin.connect.modules.beans;

public class DashboardItemModuleMeta implements ConnectModuleMeta
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
    public Class getBeanClass()
    {
        return DashboardItemModuleBean.class;
    }
}
