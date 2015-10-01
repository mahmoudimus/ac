package com.atlassian.plugin.connect.modules.beans;

public class WebSectionModuleMeta implements ConnectModuleMeta<WebSectionModuleBean>
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "webSections";
    }

    @Override
    public Class<WebSectionModuleBean> getBeanClass()
    {
        return WebSectionModuleBean.class;
    }
}
