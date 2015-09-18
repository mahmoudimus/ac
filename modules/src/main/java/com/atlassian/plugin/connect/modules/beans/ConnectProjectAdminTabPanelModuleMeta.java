package com.atlassian.plugin.connect.modules.beans;

public class ConnectProjectAdminTabPanelModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public Class getBeanClass()
    {
        return ConnectProjectAdminTabPanelModuleBean.class;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraProjectAdminTabPanels";
    }
}
