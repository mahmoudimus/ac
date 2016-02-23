package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BeanWithKeyAndParamsAndConditions;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class BeanWithKeyParamsAndConditionsBuilder<T extends BeanWithKeyParamsAndConditionsBuilder, B extends BeanWithKeyAndParamsAndConditions> extends BeanWithKeyAndParamsBuilder<T, B> {
    private List<ConditionalBean> conditions;

    public BeanWithKeyParamsAndConditionsBuilder() {
        this.conditions = newArrayList();
    }

    public BeanWithKeyParamsAndConditionsBuilder(BeanWithKeyAndParamsAndConditions defaultBean) {
        super(defaultBean);

        this.conditions = newArrayList(defaultBean.getConditions());
    }

    public T withConditions(ConditionalBean... beans) {
        return withConditions(Arrays.asList(beans));
    }

    public T withConditions(Collection<? extends ConditionalBean> beans) {
        if (beans != null) // not sure why this comes in as null sometimes
        {
            if (null == conditions) {
                conditions = newArrayList();
            }

            conditions.addAll(beans);
        }

        return (T) this;

    }

    @Override
    public T withParams(Map<String, String> params) {
        super.withParams(params);
        return (T) this;
    }

    @Override
    public T withParam(String key, String value) {
        super.withParam(key, value);
        return (T) this;
    }

    @Override
    public T withKey(String key) {
        super.withKey(key);
        return (T) this;
    }

    @Override
    public T withName(I18nProperty name) {
        super.withName(name);
        return (T) this;
    }

    @Override
    public B build() {
        return (B) new BeanWithKeyAndParamsAndConditions(this);
    }
}
