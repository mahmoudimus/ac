package com.atlassian.plugin.connect.modules.beans;

public class WorkflowPostFunctionModuleMeta implements ConnectModuleMeta
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
    public Class getBeanClass()
    {
        return WorkflowPostFunctionModuleBean.class;
    }
}
