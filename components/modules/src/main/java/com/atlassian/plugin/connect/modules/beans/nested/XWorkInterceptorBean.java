package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.builder.XWorkInterceptorBeanBuilder;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

public class XWorkInterceptorBean
{
    private String name;
    private Class<?> clazz;
    private Map<String, Object> params;

    public XWorkInterceptorBean(XWorkInterceptorBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }

    public static XWorkInterceptorBeanBuilder newXWorkInterceptorBean()
    {
        return new XWorkInterceptorBeanBuilder();
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getClazz()
    {
        return clazz;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }
}
