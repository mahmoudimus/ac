package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.NamedBean;
import com.atlassian.plugin.connect.modules.beans.builder.ControlBeanBuilder;

public class ControlBean extends NamedBean
{
    private String key;
    private String type;

    public ControlBean()
    {
        init();
    }

    public ControlBean(ControlBeanBuilder builder)
    {
        super(builder);
        init();
    }

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
    }

    public String getKey()
    {
        return key;
    }

    public String getType()
    {
        return type;
    }

    public static ControlBeanBuilder newControlBean()
    {
        return new ControlBeanBuilder();
    }

    public static ControlBeanBuilder newControlBean(ControlBean bean)
    {
        return new ControlBeanBuilder(bean);
    }

}
