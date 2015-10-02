package com.atlassian.plugin.connect.modules.beans;

public class StaticContentMacroModuleMeta implements ConnectModuleMeta<StaticContentMacroModuleBean>
{

    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public Class<StaticContentMacroModuleBean> getBeanClass()
    {
        return StaticContentMacroModuleBean.class;
    }

    @Override
    public String getDescriptorKey()
    {
        return "staticContentMacros";
    }
}
