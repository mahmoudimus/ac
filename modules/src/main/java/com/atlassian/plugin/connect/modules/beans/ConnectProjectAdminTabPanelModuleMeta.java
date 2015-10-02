package com.atlassian.plugin.connect.modules.beans;

public class ConnectProjectAdminTabPanelModuleMeta implements ConnectModuleMeta<ConnectProjectAdminTabPanelModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public Class<ConnectProjectAdminTabPanelModuleBean> getBeanClass()
    {
        return ConnectProjectAdminTabPanelModuleBean.class;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraProjectAdminTabPanels";
    }
}
