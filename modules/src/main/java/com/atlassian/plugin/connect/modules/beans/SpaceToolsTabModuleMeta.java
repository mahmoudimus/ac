package com.atlassian.plugin.connect.modules.beans;

public class SpaceToolsTabModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "spaceToolsTabs";
    }

    @Override
    public Class getBeanClass()
    {
        return SpaceToolsTabModuleBean.class;
    }
}
