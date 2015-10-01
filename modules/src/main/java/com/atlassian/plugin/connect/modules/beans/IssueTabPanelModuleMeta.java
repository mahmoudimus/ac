package com.atlassian.plugin.connect.modules.beans;

public class IssueTabPanelModuleMeta implements ConnectModuleMeta<ConnectTabPanelModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraIssueTabPanels";
    }

    @Override
    public Class<ConnectTabPanelModuleBean> getBeanClass()
    {
        return ConnectTabPanelModuleBean.class;
    }
}
