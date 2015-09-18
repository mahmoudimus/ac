package com.atlassian.plugin.connect.modules.beans;

public class DynamicContentMacroModuleMeta implements ConnectModuleMeta
{
    public static final String DESCRIPTOR_KEY = "dynamicContentMacros";
    public static final Class BEAN_CLASS = DynamicContentMacroModuleBean.class;
    
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }
}
