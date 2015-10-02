package com.atlassian.plugin.connect.modules.beans;

public class SearchRequestViewModuleMeta implements ConnectModuleMeta<SearchRequestViewModuleBean>
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
    public Class<SearchRequestViewModuleBean> getBeanClass()
    {
        return SearchRequestViewModuleBean.class;
    }
}
