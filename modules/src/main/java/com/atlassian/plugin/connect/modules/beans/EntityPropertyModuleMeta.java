package com.atlassian.plugin.connect.modules.beans;

public class EntityPropertyModuleMeta implements ConnectModuleMeta<EntityPropertyModuleBean>
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
    public Class<EntityPropertyModuleBean> getBeanClass()
    {
        return EntityPropertyModuleBean.class;
    }
}
