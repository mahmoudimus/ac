package com.atlassian.plugin.connect.modules.beans;

public class WorkflowPostFunctionModuleMeta implements ConnectModuleMeta<WorkflowPostFunctionModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraWorkflowPostFunctions";
    }

    @Override
    public Class<WorkflowPostFunctionModuleBean> getBeanClass()
    {
        return WorkflowPostFunctionModuleBean.class;
    }
}
