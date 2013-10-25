package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BeanWithKeyAndParamsBuilder;

import static com.google.common.collect.Maps.newHashMap;

public class BeanWithKeyAndParams extends NameToKeyBean
{
    private Map<String, String> params;

    public BeanWithKeyAndParams()
    {
        this.params = newHashMap();
    }

    public BeanWithKeyAndParams(BeanWithKeyAndParamsBuilder builder)
    {
        super(builder);

        if (null == params)
        {
            this.params = newHashMap();
        }
    }

    public Map<String, String> getParams()
    {
        return params;
    }
}
