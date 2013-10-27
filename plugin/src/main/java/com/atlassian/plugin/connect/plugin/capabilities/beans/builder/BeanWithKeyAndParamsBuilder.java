package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BeanWithKeyAndParams;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class BeanWithKeyAndParamsBuilder<T extends BeanWithKeyAndParamsBuilder, B extends BeanWithKeyAndParams> extends NameToKeyBeanBuilder<T,B>
{
    private Map<String, String> params;

    public BeanWithKeyAndParamsBuilder()
    {
        this.params = newHashMap();
    }

    public BeanWithKeyAndParamsBuilder(BeanWithKeyAndParams defaultBean)
    {
        super(defaultBean);

        this.params = defaultBean.getParams();
    }

    public T withParams(Map<String, String> params)
    {
        checkNotNull(params);

        this.params = params;
        return (T) this;
    }

    public T withParam(String key, String value)
    {
        params.put(key,value);
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new BeanWithKeyAndParams(this);
    }
}
