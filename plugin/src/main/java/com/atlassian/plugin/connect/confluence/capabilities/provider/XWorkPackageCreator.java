package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkResultBean;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationUtil;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.InterceptorConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;
import com.opensymphony.xwork.config.entities.ResultTypeConfig;
import com.opensymphony.xwork.interceptor.Interceptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.opensymphony.xwork.config.providers.InterceptorBuilder.constructInterceptorReference;
import static java.util.Collections.emptyMap;

/**
 * This class is responsible for taking an XWorkActionModuleBean and creating a fully-formed XWork PackageConfig with
 * it
 */
public class XWorkPackageCreator
{
    private final ConnectAddonBean addon;
    private final Plugin theConnectPlugin;
    private final XWorkActionModuleBean actionModuleBean;

    public XWorkPackageCreator(final ConnectAddonBean addon, Plugin theConnectPlugin, final XWorkActionModuleBean actionModuleBean)
    {
        this.addon = addon;
        this.theConnectPlugin = theConnectPlugin;
        this.actionModuleBean = actionModuleBean;
    }

    public void createAndRegister(Configuration configuration)
    {
        String namespace = actionModuleBean.getNamespace();

        String packageName = "atlassian-connect-" + addon.getKey() + "-" + actionModuleBean.getRawKey();

        PackageConfig packageConfig = new PackageConfig(packageName, namespace, false, null);

        addParentPackages(packageConfig, configuration);
        addResultTypes(packageConfig, actionModuleBean);
        addInterceptors(packageConfig, actionModuleBean);

        ActionConfig actionConfig = buildActionConfig(theConnectPlugin, actionModuleBean);

        actionConfig.addInterceptors(buildActionInterceptors(packageConfig, actionModuleBean));
        actionConfig.setResults(buildResults(packageConfig, actionModuleBean));

        packageConfig.addActionConfig(actionModuleBean.getRawKey(), actionConfig);

        configuration.addPackageConfig(actionModuleBean.getRawKey(), packageConfig);
    }

    private void addParentPackages(PackageConfig packageConfig, Configuration configuration)
    {
        List<?> parentStack = ConfigurationUtil.buildParentsFromString(configuration, "default");
        for (Object parent : parentStack)
        {
            packageConfig.addParent((PackageConfig) parent);
        }
    }

    private void addResultTypes(PackageConfig packageConfig, XWorkActionModuleBean actionModuleBean)
    {
        for (Map.Entry<String, Class<?>> resultType : actionModuleBean.getResultTypes().entrySet())
        {
            packageConfig.addResultTypeConfig(new ResultTypeConfig(resultType.getKey(), resultType.getValue()));
        }
    }

    private void addInterceptors(PackageConfig packageConfig, XWorkActionModuleBean actionModuleBean)
    {
        for (XWorkInterceptorBean interceptorBean : actionModuleBean.getInterceptorsBeans())
        {
            InterceptorConfig interceptorConfig = new InterceptorConfig(interceptorBean.getName(),
                    interceptorBean.getClazz(), interceptorBean.getParams());
            packageConfig.addInterceptorConfig(interceptorConfig);
        }
    }

    private ActionConfig buildActionConfig(Plugin plugin, XWorkActionModuleBean actionModuleBean)
    {
        Class clazz = actionModuleBean.getClazz();
        Map<String, Object> params = actionModuleBean.getParameters();

        return new PluginAwareActionConfig(null, clazz.getName(), params, emptyMap(), newArrayList(), plugin);
    }

    private Map<String, ResultConfig> buildResults(PackageConfig packageConfig, XWorkActionModuleBean actionModuleBean)
    {
        Map<String, ResultConfig> results = newHashMap();
        Map<?, ?> resultTypeConfigs = packageConfig.getAllResultTypeConfigs();

        for (XWorkResultBean resultBean : actionModuleBean.getResultBeans())
        {
            ResultTypeConfig resultTypeConfig = (ResultTypeConfig) resultTypeConfigs.get(resultBean.getType());
            String name = resultBean.getName();
            results.put(name, new ResultConfig(name, resultTypeConfig.getClazz(), resultBean.getParams()));
        }

        return results;
    }

    private List<Interceptor> buildActionInterceptors(PackageConfig packageConfig, XWorkActionModuleBean actionModuleBean)
    {
        List<Interceptor> interceptors = newArrayList();

        for (String interceptorRef : actionModuleBean.getInterceptorRefs())
        {
            List list = constructInterceptorReference(packageConfig, interceptorRef, Collections.EMPTY_MAP);
            for (Object interceptor : list)
            {
                interceptors.add((Interceptor) interceptor);
            }
        }

        return interceptors;
    }
}
