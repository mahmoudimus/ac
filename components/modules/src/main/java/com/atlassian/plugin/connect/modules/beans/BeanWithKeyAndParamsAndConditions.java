package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BeanWithKeyParamsAndConditionsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class BeanWithKeyAndParamsAndConditions extends BeanWithKeyAndParams
{
    /**
     * <a href="../../concepts/conditions.html">Conditions</a> can be added to display only when all the given conditions are true.
     */
    private List<ConditionalBean> conditions;

    public BeanWithKeyAndParamsAndConditions()
    {
        this.conditions = newArrayList();
    }

    public BeanWithKeyAndParamsAndConditions(BeanWithKeyParamsAndConditionsBuilder builder)
    {
        super(builder);

        if (null == conditions)
        {
            this.conditions = newArrayList();
        }
    }

    public List<ConditionalBean> getConditions()
    {
        return conditions;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof BeanWithKeyAndParamsAndConditions && super.equals(otherObj)))
        {
            return false;
        }

        BeanWithKeyAndParamsAndConditions other = (BeanWithKeyAndParamsAndConditions) otherObj;

        return new EqualsBuilder()
                .append(conditions, other.conditions)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(59, 29)
                .appendSuper(super.hashCode())
                .append(conditions)
                .build();
    }
}
