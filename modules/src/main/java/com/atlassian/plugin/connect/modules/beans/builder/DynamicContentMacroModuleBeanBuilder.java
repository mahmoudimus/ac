package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;

public class DynamicContentMacroModuleBeanBuilder extends BaseContentMacroModuleBeanBuilder<DynamicContentMacroModuleBeanBuilder, DynamicContentMacroModuleBean>
{
    private String width;
    private String height;
    private MacroRenderModesBean renderModes;

    public DynamicContentMacroModuleBeanBuilder()
    {
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

    public DynamicContentMacroModuleBeanBuilder withRenderModes(MacroRenderModesBean renderModes)
    {
        this.renderModes = renderModes;
        return this;
    }

    @Override
    public DynamicContentMacroModuleBean build()
    {
        return new DynamicContentMacroModuleBean(this);
    }

}
