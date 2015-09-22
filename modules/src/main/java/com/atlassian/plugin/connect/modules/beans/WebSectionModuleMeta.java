package com.atlassian.plugin.connect.modules.beans;

public class WebSectionModuleMeta implements ConnectModuleMeta
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
    public Class getBeanClass()
    {
        return WebSectionModuleBean.class;
    }
}
