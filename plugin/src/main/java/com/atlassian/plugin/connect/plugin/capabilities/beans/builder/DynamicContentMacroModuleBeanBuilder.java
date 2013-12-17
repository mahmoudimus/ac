package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;

public class DynamicContentMacroModuleBeanBuilder extends BaseContentMacroModuleBeanBuilder<DynamicContentMacroModuleBeanBuilder, DynamicContentMacroModuleBean>
{
    public DynamicContentMacroModuleBeanBuilder()
    {
    }

    public DynamicContentMacroModuleBeanBuilder(DynamicContentMacroModuleBean defaultBean)
    {
        super(defaultBean);
    }

    @Override
    public DynamicContentMacroModuleBean build()
    {
        return new DynamicContentMacroModuleBean(this);
    }
}
