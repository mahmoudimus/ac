package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Arrays;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionType;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @since 1.0
 */
public class CompositeConditionBeanBuilder extends BaseCapabilityBeanBuilder<CompositeConditionBeanBuilder,CompositeConditionBean>
{
    private List<ConditionalBean> conditions;
    private CompositeConditionType type;

    public CompositeConditionBeanBuilder()
    {
        this.conditions = newArrayList();
        this.type = CompositeConditionType.and;
    }

    public CompositeConditionBeanBuilder(CompositeConditionBean defaultBean)
    {
        this.conditions = defaultBean.getConditions();
        this.type = defaultBean.getType();
    }

    public CompositeConditionBeanBuilder withConditions(List<ConditionalBean> conditions)
    {
        this.conditions = conditions;
        return this;
    }

    public CompositeConditionBeanBuilder withConditions(ConditionalBean ... conditions)
    {
        this.conditions.addAll(Arrays.asList(conditions));
        return this;
    }

    public CompositeConditionBeanBuilder withType(CompositeConditionType type)
    {
        this.type = type;
        return this;
    }
    
    @Override
    public CompositeConditionBean build()
    {
        return new CompositeConditionBean(this);
    }
}
