package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BeanWithKeyAndParamsAndConditions;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

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
    public T withParams(Map<String, String> params)
    {
        super.withParams(params);
        return (T) this;
    }

    @Override
    public T withParam(String key, String value)
    {
        super.withParam(key, value);
        return (T) this;
    }

    @Override
    public T withKey(String key)
    {
        super.withKey(key);
        return (T) this;
    }

    @Override
    public T withName(I18nProperty name)
    {
        super.withName(name);
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new BeanWithKeyAndParamsAndConditions(this);
    }
}
