package com.atlassian.plugin.connect.modules.beans;

public class ProjectTabPanelModuleMeta implements ConnectModuleMeta<ConnectTabPanelModuleBean>
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
    public Class<ConnectTabPanelModuleBean> getBeanClass()
    {
        return ConnectTabPanelModuleBean.class;
    }
}
