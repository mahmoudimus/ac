package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BeanWithParamsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class BeanWithParams extends BaseModuleBean
{
    /**
     * This object represents a map of key/value pairs, where each property name and value corresponds to the parameter name and value respectively.
     *
     *#### Example
     *
     * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#PARAMS_EXAMPLE}
     * @schemaTitle Object
     */
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
