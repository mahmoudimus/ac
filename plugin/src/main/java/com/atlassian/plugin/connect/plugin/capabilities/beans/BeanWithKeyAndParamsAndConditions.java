package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BeanWithKeyParamsAndConditionsBuilder;

import static com.google.common.collect.Lists.newArrayList;

public class BeanWithKeyAndParamsAndConditions extends BeanWithKeyAndParams
{
    private List<ConditionalBean> conditions;

    public BeanWithKeyAndParamsAndConditions()
    {
        this.conditions = newArrayList();
    }

    public BeanWithKeyAndParamsAndConditions(BeanWithKeyParamsAndConditionsBuilder builder)
    {
        super(builder);

        if(null == conditions)
        {
            this.conditions = newArrayList();
        }
    }

    public List<ConditionalBean> getConditions()
    {
        return conditions;
    }
}
