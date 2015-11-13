package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;

public class ControlPanelBean extends BaseModuleBean
{

    public String getKey()
    {
        return key;
    }

    public String getType()
    {
        return type;
    }

    public String getText()
    {
        return text;
    }

    public ActionTargetBean getTarget()
    {
        return target;
    }

    /**
     * The URL to the macro configuration page in the add-on.
     */
    @Required
    private String key;

    private String type;

    private String text;

    private ActionTargetBean target;

    private void init()
    {
        if (null == key)
        {
            key = "";
        }
        if (null == type)
        {
            type = "";
        }
        if (null == text)
        {
            text = "";
        }
    }

    public boolean hasActionTarget()
    {
        return target != null;
    }

    public ActionTargetBean getActionTarget()
    {
        return target;
    }
}
