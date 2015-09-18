package com.atlassian.plugin.connect.modules.beans;

public class StaticContentMacroModuleMeta implements ConnectModuleMeta
{
    public static final String DESCRIPTOR_KEY = "staticContentMacros";
    public static final Class BEAN_CLASS = StaticContentMacroModuleBean.class;
    
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