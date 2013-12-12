package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BeanWithKeyAndParamsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

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

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof BeanWithKeyAndParams && super.equals(otherObj)))
        {
            return false;
        }

        BeanWithKeyAndParams other = (BeanWithKeyAndParams) otherObj;

        return new EqualsBuilder()
                .append(params, other.params)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(41, 17)
                .append(super.hashCode())
                .append(params)
                .build();
    }
}
