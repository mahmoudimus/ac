package com.atlassian.plugin.connect.modules.beans;

public class ReportModuleMeta implements ConnectModuleMeta<ReportModuleBean>
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
    public Class<ReportModuleBean> getBeanClass()
    {
        return ReportModuleBean.class;
    }
}
