package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BeanWithParams;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SingleConditionBeanBuilder;

import com.google.common.base.Objects;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @since 1.0
 */
public class SingleConditionBean extends BeanWithParams implements ConditionalBean
{
    private String condition;
    private Boolean invert;

    public SingleConditionBean()
    {
        this.condition = "";
        this.invert = false;
    }

    public SingleConditionBean(SingleConditionBeanBuilder builder)
    {
        super(builder);

        if (null == condition)
        {
            this.condition = "";
        }
        if (null == invert)
        {
            this.invert = false;
        }
    }

    public String getCondition()
    {
        return condition;
    }

    public Boolean isInvert()
    {
        return invert;
    }

    public static SingleConditionBeanBuilder newSingleConditionBean()
    {
        return new SingleConditionBeanBuilder();
    }

    public static SingleConditionBeanBuilder newSingleConditionBean(SingleConditionBean defaultBean)
    {
        return new SingleConditionBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(condition, invert);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof SingleConditionBean))
        {
            return false;
        }
        else
        {
            final SingleConditionBean that = (SingleConditionBean) obj;
            return Objects.equal(condition, that.condition) &&
                    Objects.equal(invert, that.invert);
        }
    }
}
