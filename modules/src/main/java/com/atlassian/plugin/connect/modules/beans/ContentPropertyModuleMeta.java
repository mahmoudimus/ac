package com.atlassian.plugin.connect.modules.beans;

public class ContentPropertyModuleMeta implements ConnectModuleMeta<ContentPropertyModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "confluenceContentProperties";
    }

    @Override
    public Class<ContentPropertyModuleBean> getBeanClass()
    {
        return ContentPropertyModuleBean.class;
    }
}
