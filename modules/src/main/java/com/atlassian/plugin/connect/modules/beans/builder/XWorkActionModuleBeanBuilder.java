package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkResultBean;

import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.nested.XWorkResultBean.newXWorkResultBean;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class XWorkActionModuleBeanBuilder
        extends RequiredKeyBeanBuilder<XWorkActionModuleBeanBuilder, XWorkActionModuleBean>
{
    private static final String DEFAULT_VALIDATING_STACK = "validatingStack";
    private static final String VELOCITY_RESULT_TYPE = "velocity";

    private String namespace;
    private Class<?> clazz;
    private Map<String, Object> parameters = newHashMap();
    private List<String> interceptorRefs = newArrayList();
    private List<XWorkInterceptorBean> interceptorsBeans = newArrayList();
    private Map<String, Class<?>> resultTypes = newHashMap();
    private List<XWorkResultBean> resultBeans = newArrayList();

    public XWorkActionModuleBeanBuilder withNamespace(String namespace)
    {
        this.namespace = namespace;
        return this;
    }

    public XWorkActionModuleBeanBuilder withClazz(Class<?> clazz)
    {
        this.clazz = clazz;
        return this;
    }

    public XWorkActionModuleBeanBuilder withParameter(String key, Object value)
    {
        parameters.put(key, value);
        return this;
    }

    public XWorkActionModuleBeanBuilder withDefaultValidatingInterceptorStack()
    {
        return withInterceptorRef(DEFAULT_VALIDATING_STACK);
    }

    public XWorkActionModuleBeanBuilder withInterceptor(XWorkInterceptorBean interceptorBean)
    {
        interceptorsBeans.add(interceptorBean);
        return withInterceptorRef(interceptorBean.getName());
    }

    public XWorkActionModuleBeanBuilder withInterceptorRef(String interceptorRef)
    {
        interceptorRefs.add(interceptorRef);
        return this;
    }

    public XWorkActionModuleBeanBuilder withResult(XWorkResultBean resultBean)
    {
        resultBeans.add(resultBean);
        return this;
    }

    public XWorkActionModuleBeanBuilder withResultType(String name, Class<?> clazz)
    {
        resultTypes.put(name, clazz);
        return this;
    }


    public XWorkActionModuleBeanBuilder withVelocityResult(String name, String templateLocation)
    {
        return withResult(newXWorkResultBean()
                .withName(name)
                .withType(VELOCITY_RESULT_TYPE)
                .withParam("location", templateLocation)
                .build());
    }

    public XWorkActionModuleBean build()
    {
        return new XWorkActionModuleBean(this);
    }
}
