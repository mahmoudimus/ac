package com.atlassian.plugin.connect.modules.beans;

public class DynamicContentMacroModuleMeta implements ConnectModuleMeta<DynamicContentMacroModuleBean>
{

    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public Class<DynamicContentMacroModuleBean> getBeanClass()
    {
        return DynamicContentMacroModuleBean.class;
    }

    @Override
    public String getDescriptorKey()
    {
        return "dynamicContentMacros";
    }
}
