package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BeanWithParams;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class BeanWithParamsBuilder<T extends BeanWithParamsBuilder, B extends BeanWithParams> extends BaseCapabilityBeanBuilder<T,B>
{
    private Map<String, String> params;

    public BeanWithParamsBuilder()
    {
        this.params = newHashMap();
    }

    public BeanWithParamsBuilder(BeanWithParams defaultBean)
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
        return (B) new BeanWithParams(this);
    }
}
