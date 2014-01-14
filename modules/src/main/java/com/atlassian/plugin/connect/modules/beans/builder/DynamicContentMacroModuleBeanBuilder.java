package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;

public class DynamicContentMacroModuleBeanBuilder extends BaseContentMacroModuleBeanBuilder<DynamicContentMacroModuleBeanBuilder, DynamicContentMacroModuleBean>
{
    private String width;
    private String height;

    public DynamicContentMacroModuleBeanBuilder()
    {
    }

    public DynamicContentMacroModuleBeanBuilder(DynamicContentMacroModuleBean defaultBean)
    {
        super(defaultBean);
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
    }

    public DynamicContentMacroModuleBeanBuilder withWidth(String width)
    {
        this.width = width;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withHeight(String height)
    {
        this.height = height;
        return this;
    }

    @Override
    public DynamicContentMacroModuleBean build()
    {
        return new DynamicContentMacroModuleBean(this);
    }
}
