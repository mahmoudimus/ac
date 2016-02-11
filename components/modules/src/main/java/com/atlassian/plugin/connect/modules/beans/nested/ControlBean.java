package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.modules.beans.builder.ControlBeanBuilder;

public class ControlBean extends RequiredKeyBean
{
    @Required
    @StringSchemaAttributes(pattern = "^[a-zA-Z0-9-]+$")
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
        if (null == type)
        {
            type = "";
        }
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
