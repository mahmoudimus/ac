package com.atlassian.plugin.connect.modules.beans;

public class WebItemModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "webItems";
    }

    @Override
    public Class getBeanClass()
    {
        return WebItemModuleBean.class;
    }
}
