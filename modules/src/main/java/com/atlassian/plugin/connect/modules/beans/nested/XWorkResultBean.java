package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.builder.XWorkResultBeanBuilder;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

public class XWorkResultBean
{
    private String name;
    private String type;
    private Map<String, Object> params;

    public XWorkResultBean(final XWorkResultBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public static XWorkResultBeanBuilder newXWorkResultBean()
    {
        return new XWorkResultBeanBuilder();
    }
}
