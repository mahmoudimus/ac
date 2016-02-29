package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class XWorkInterceptorBeanBuilder {
    private String name;
    private Class<?> clazz;
    private Map<String, Object> params = newHashMap();

    public XWorkInterceptorBeanBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public XWorkInterceptorBeanBuilder withClazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    public XWorkInterceptorBeanBuilder withParam(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public XWorkInterceptorBean build() {
        return new XWorkInterceptorBean(this);
    }
}
