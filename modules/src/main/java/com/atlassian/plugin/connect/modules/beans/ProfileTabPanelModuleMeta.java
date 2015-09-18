package com.atlassian.plugin.connect.modules.beans;

public class ProfileTabPanelModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraProfileTabPanels";
    }

    @Override
    public Class getBeanClass()
    {
        return ConnectTabPanelModuleBean.class;
    }
}
