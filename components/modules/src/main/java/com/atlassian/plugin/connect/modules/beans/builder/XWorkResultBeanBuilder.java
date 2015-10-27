package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.XWorkResultBean;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class XWorkResultBeanBuilder
{
    private String name;
    private String type;
    private Map<String, Object> params = newHashMap();

    public XWorkResultBeanBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public XWorkResultBeanBuilder withType(String type)
    {
        this.type = type;
        return this;
    }

    public XWorkResultBeanBuilder withParam(String key, Object value)
    {
        params.put(key, value);
        return this;
    }

    public XWorkResultBean build()
    {
        return new XWorkResultBean(this);
    }
}
