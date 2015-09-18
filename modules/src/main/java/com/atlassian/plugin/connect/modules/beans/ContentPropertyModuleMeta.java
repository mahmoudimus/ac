package com.atlassian.plugin.connect.modules.beans;

public class ContentPropertyModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "contentProperties";
    }

    @Override
    public Class getBeanClass()
    {
        return ContentPropertyModuleBean.class;
    }
}
