package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;

/**
 * @since 1.0
 */
public class SingleConditionBeanBuilder extends BeanWithParamsBuilder<SingleConditionBeanBuilder, SingleConditionBean>
{
    private String condition;
    private Boolean invert;

    public SingleConditionBeanBuilder()
    {
    }

    public SingleConditionBeanBuilder(SingleConditionBean defaultBean)
    {
        this.condition = defaultBean.getCondition();
        this.invert = defaultBean.isInvert();
    }

    public SingleConditionBeanBuilder withCondition(String condition)
    {
        this.condition = condition;
        return this;
    }

    public SingleConditionBeanBuilder withInvert(Boolean invert)
    {
        this.invert = invert;
        return this;
    }

    @Override
    public SingleConditionBean build()
    {
        return new SingleConditionBean(this);
    }
}
