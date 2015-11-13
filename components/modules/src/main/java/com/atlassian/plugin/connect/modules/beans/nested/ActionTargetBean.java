package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;

public class ActionTargetBean extends BaseModuleBean
{
    private String onClick;

    public boolean hasOnClick()
    {
        return onClick != null;
    }

    public String getOnClick()
    {
        return onClick;
    }
}
