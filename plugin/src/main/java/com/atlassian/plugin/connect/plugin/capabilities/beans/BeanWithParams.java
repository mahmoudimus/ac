package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BeanWithParamsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class BeanWithParams extends BaseModuleBean
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

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof BeanWithParams))
        {
            return false;
        }

        BeanWithParams other = (BeanWithParams) otherObj;

        return new EqualsBuilder()
                .append(params, other.params)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(47, 29)
                .append(params)
                .build();
    }
}
