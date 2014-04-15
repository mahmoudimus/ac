package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MacroRenderModeBeanBuilder;

public class MacroRenderModeBean extends BaseModuleBean {

    @Required
    private MacroRenderModeType type;

    public MacroRenderModeType getRenderModeType()
    {
        return type;
    }


    private void init()
    {
        if (null == type)
        {
            type = MacroRenderModeType.desktop;
        }
    }

    public MacroRenderModeBean(MacroRenderModeBeanBuilder builder)
    {
        super(builder);
        init();
    }


}
