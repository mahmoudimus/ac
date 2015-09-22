package com.atlassian.plugin.connect.modules.beans;

public class ReportModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraReports";
    }

    @Override
    public Class getBeanClass()
    {
        return ReportModuleBean.class;
    }
}
