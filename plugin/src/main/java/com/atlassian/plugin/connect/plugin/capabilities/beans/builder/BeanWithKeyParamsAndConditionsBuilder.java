package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Arrays;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BeanWithKeyAndParamsAndConditions;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;

import static com.google.common.collect.Lists.newArrayList;

public class BeanWithKeyParamsAndConditionsBuilder<T extends BeanWithKeyParamsAndConditionsBuilder, B extends BeanWithKeyAndParamsAndConditions> extends BeanWithKeyAndParamsBuilder<T,B>
{
    private List<ConditionalBean> conditions;
    
    public BeanWithKeyParamsAndConditionsBuilder()
    {
        this.conditions = newArrayList();
    }

    public BeanWithKeyParamsAndConditionsBuilder(BeanWithKeyAndParamsAndConditions defaultBean)
    {
        super(defaultBean);
        
        this.conditions = defaultBean.getConditions();
    }

    public T withConditions(ConditionalBean ... beans)
    {
        if(null == conditions)
        {
            conditions = newArrayList();
        }

        conditions.addAll(Arrays.asList(beans));

        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new BeanWithKeyAndParamsAndConditions(this);
    }
}
