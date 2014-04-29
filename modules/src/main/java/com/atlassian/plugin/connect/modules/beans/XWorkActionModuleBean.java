package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.XWorkActionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkResultBean;

import java.util.List;
import java.util.Map;

/**
 * Models an XWork action contained in its own XWork Package. Includes support for modelling result types and
 * interceptors.
 *
 * Note that this bean is not publicly exposed, and thus does not contain user-facing javadocs.
 */
public class XWorkActionModuleBean extends RequiredKeyBean
{
    private String namespace;
    private Class<?> clazz;
    private Map<String, Object> parameters;
    private List<String> interceptorRefs;
    private List<XWorkInterceptorBean> interceptorsBeans;
    private Map<String, Class<?>> resultTypes;
    private List<XWorkResultBean> resultBeans;

    public XWorkActionModuleBean(XWorkActionModuleBeanBuilder builder)
    {
        super(builder);
    }

    public String getNamespace()
    {
        return namespace;
    }

    public Class<?> getClazz()
    {
        return clazz;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public List<String> getInterceptorRefs()
    {
        return interceptorRefs;
    }

    public List<XWorkInterceptorBean> getInterceptorsBeans()
    {
        return interceptorsBeans;
    }

    public Map<String, Class<?>> getResultTypes()
    {
        return resultTypes;
    }

    public List<XWorkResultBean> getResultBeans()
    {
        return resultBeans;
    }

    /**
     * Builds a context-relative URL to the action modelled by this bean.
     */
    public String getUrl()
    {
        return namespace + "/" + getRawKey() + ".action";
    }

    public static XWorkActionModuleBeanBuilder newXWorkActionBean()
    {
        return new XWorkActionModuleBeanBuilder();
    }
}
