package com.atlassian.plugin.connect.modules.beans;

public class EntityPropertyModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "jiraEntityProperties";
    }

    @Override
    public Class getBeanClass()
    {
        return EntityPropertyModuleBean.class;
    }
}
