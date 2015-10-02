package com.atlassian.plugin.connect.modules.beans;

public class ProfileTabPanelModuleMeta implements ConnectModuleMeta<ConnectTabPanelModuleBean>
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
    public Class<ConnectTabPanelModuleBean> getBeanClass()
    {
        return ConnectTabPanelModuleBean.class;
    }
}
