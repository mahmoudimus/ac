package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeType;

import java.util.HashMap;
import java.util.Map;

public class DynamicContentMacroModuleBeanBuilder extends BaseContentMacroModuleBeanBuilder<DynamicContentMacroModuleBeanBuilder, DynamicContentMacroModuleBean>
{
    private String width;
    private String height;
    private Map<MacroRenderModeType, EmbeddedStaticContentMacroBean> renderModes = new HashMap<MacroRenderModeType, EmbeddedStaticContentMacroBean>();

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

    public DynamicContentMacroModuleBeanBuilder withRenderMode(MacroRenderModeType type, EmbeddedStaticContentMacroBean embeddedStaticContentMacroBean)
    {
        renderModes.put(type, embeddedStaticContentMacroBean);
        return this;
    }

    @Override
    public DynamicContentMacroModuleBean build()
    {
        return new DynamicContentMacroModuleBean(this);
    }

}
