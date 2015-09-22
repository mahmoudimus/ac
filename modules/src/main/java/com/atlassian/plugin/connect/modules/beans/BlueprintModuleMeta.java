package com.atlassian.plugin.connect.modules.beans;

public class BlueprintModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "blueprints";
    }

    @Override
    public Class getBeanClass()
    {
        return BlueprintModuleBean.class;
    }
}
