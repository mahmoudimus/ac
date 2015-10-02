package com.atlassian.plugin.connect.modules.beans;

public class BlueprintModuleMeta implements ConnectModuleMeta<BlueprintModuleBean>
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
    public Class<BlueprintModuleBean> getBeanClass()
    {
        return BlueprintModuleBean.class;
    }
}
