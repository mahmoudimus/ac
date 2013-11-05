package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BeanWithParamsBuilder;

import static com.google.common.collect.Maps.newHashMap;

public class BeanWithParams extends BaseCapabilityBean
{
    private Map<String, String> params;

    public BeanWithParams()
    {
        this.params = newHashMap();
    }

    public BeanWithParams(BeanWithParamsBuilder builder)
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
