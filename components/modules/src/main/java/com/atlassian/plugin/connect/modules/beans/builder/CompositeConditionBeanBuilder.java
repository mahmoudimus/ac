package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @since 1.0
 */
public class CompositeConditionBeanBuilder extends BaseModuleBeanBuilder<CompositeConditionBeanBuilder, CompositeConditionBean>
{
    private List<ConditionalBean> conditions;
    private CompositeConditionType type;

    public CompositeConditionBeanBuilder()
    {
        this.conditions = newArrayList();
        this.type = CompositeConditionType.AND;
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

    public CompositeConditionBeanBuilder withConditions(ConditionalBean... conditions)
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
