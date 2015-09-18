package com.atlassian.plugin.connect.modules.beans;

public class SearchRequestViewModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraSearchRequestViews";
    }

    @Override
    public Class getBeanClass()
    {
        return SearchRequestViewModuleBean.class;
    }
}
