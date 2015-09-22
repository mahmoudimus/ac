package com.atlassian.plugin.connect.modules.beans;

public class GeneralPageModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "generalPages";
    }

    @Override
    public Class getBeanClass()
    {
        return ConnectPageModuleBean.class;
    }
}
