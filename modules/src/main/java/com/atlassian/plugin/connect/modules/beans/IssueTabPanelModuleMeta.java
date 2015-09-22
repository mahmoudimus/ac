package com.atlassian.plugin.connect.modules.beans;

public class IssueTabPanelModuleMeta implements ConnectModuleMeta
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
    public Class getBeanClass()
    {
        return ConnectTabPanelModuleBean.class;
    }
}
