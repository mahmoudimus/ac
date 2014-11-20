package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeType;

public class MacroRenderModeBeanBuilder extends BaseModuleBeanBuilder<MacroRenderModeBeanBuilder, MacroRenderModeBean> {
    private MacroRenderModeType type;
    private StaticContentMacroModuleBean macro;



    public MacroRenderModeBeanBuilder()
    {
    }

    public MacroRenderModeBeanBuilder(MacroRenderModeBean defaultBean)
    {
        this.type = defaultBean.getRenderModeType();
    }

    @Override
    public MacroRenderModeBean build()
    {
        return new MacroRenderModeBean(this);
    }
}
