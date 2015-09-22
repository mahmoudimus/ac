package com.atlassian.plugin.connect.modules.beans;

public class ProjectTabPanelModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraProjectTabPanels";
    }

    @Override
    public Class getBeanClass()
    {
        return ConnectTabPanelModuleBean.class;
    }
}
