package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BeanWithKeyAndParamsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class BeanWithKeyAndParams extends RequiredKeyBean
{
    /**
     * This object represents a map of key/value pairs, where each property name and value corresponds to the parameter name and value respectively.
     *
     *#### Example
     *
     * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#PARAMS_EXAMPLE}
     * @schemaTitle Object
     */
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
                .appendSuper(super.hashCode())
                .append(params)
                .build();
    }
}
