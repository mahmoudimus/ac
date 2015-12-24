package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.connect.modules.beans.nested.AddonScopeBean;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class AddonScopeApiPathBuilder
{
    Collection<RestApiScopeHelper.RestScope> restResources = new ArrayList<>();
    Collection<RpcEncodedSoapApiScopeHelper> soapResources = new ArrayList<>();
    Collection<JsonRpcApiScopeHelper>        jsonResources = new ArrayList<>();
    Collection<XmlRpcApiScopeHelper>         xmlResources = new ArrayList<>();
    Collection<PathScopeHelper>              paths         = new ArrayList<>();

    public AddonScopeApiPathBuilder withRestPaths(AddonScopeBean.RestPathBean restPathBean, Collection<String> methods)
    {
        for (String basePath : restPathBean.getBasePaths())
        {
            restResources.add(new RestApiScopeHelper.RestScope(restPathBean.getName(), restPathBean.getVersions(), basePath, methods, true));
        }

        return this;
    }

    public AddonScopeApiPathBuilder withSoapRpcResources(AddonScopeBean.SoapRpcPathBean soapRpcPathBean, Collection<String> httpMethods)
    {
        for (String path : soapRpcPathBean.getPaths())
        {
            for (String httpMethod : httpMethods)
            {
                soapResources.add(new RpcEncodedSoapApiScopeHelper("/rpc/soap" + prefixWithSlash(path), "http://soap.rpc.jira.atlassian.com", soapRpcPathBean.getRpcMethods(), httpMethod));
            }
        }
        return this;
    }

    public AddonScopeApiPathBuilder withJsonRpcResources(AddonScopeBean.JsonRpcPathBean jsonRpcPathBean, Collection<String> httpMethods)
    {
        for (String path : jsonRpcPathBean.getPaths())
        {
            for (String httpMethod : httpMethods)
            {
                jsonResources.add(new JsonRpcApiScopeHelper("/rpc/json-rpc" + prefixWithSlash(path), jsonRpcPathBean.getRpcMethods(), httpMethod));
            }
        }
        return this;
    }

    public AddonScopeApiPathBuilder withXmlRpcResources(AddonScopeBean.XmlRpcBean xmlRpcBean)
    {
        for (String prefix : xmlRpcBean.getPrefixes())
        {
            xmlResources.add(new XmlRpcApiScopeHelper("/rpc/xmlrpc", prefixXmlRpcMethods(xmlRpcBean.getRpcMethods(), prefix)));
        }
        return this;
    }

    public AddonScopeApiPathBuilder withPaths(AddonScopeBean.PathBean path)
    {
        paths.add(new PathScopeHelper(true, path.getPaths()));
        return this;
    }

    public AddonScopeApiPathBuilder withPaths(Iterable<AddonScopeApiPath> paths)
    {
        for (AddonScopeApiPath path : paths)
        {
            path.addTo(restResources, soapResources, jsonResources, xmlResources, this.paths);
        }

        return this;
    }

    public Collection<AddonScopeApiPath> build()
    {
        Collection<AddonScopeApiPath> paths = new ArrayList<>();

        if (!restResources.isEmpty())
        {
            paths.add(new AddonScopeApiPath.RestApiPath(restResources));
        }

        if (!soapResources.isEmpty())
        {
            paths.add(new AddonScopeApiPath.SoapRpcApiPath(soapResources));
        }

        if (!jsonResources.isEmpty())
        {
            paths.add(new AddonScopeApiPath.JsonRpcApiPath(jsonResources));
        }

        if (!xmlResources.isEmpty())
        {
            paths.add(new AddonScopeApiPath.XmlRpcApiPath(xmlResources));
        }

        if (!this.paths.isEmpty())
        {
            paths.add(new AddonScopeApiPath.ApiPath(this.paths));
        }

        return paths;
    }

    private static String prefixWithSlash(String path)
    {
        return null == path
            ? null
            : path.startsWith("/")
                ? path
                : "/" + path;
    }

    private static Collection<String> prefixXmlRpcMethods(Collection<String> rpcMethods, final String prefix)
    {
        return Collections2.transform(rpcMethods, new Function<String, String>()
        {
            @Override
            public String apply(@Nullable String method)
            {
                return prefix + "." + method;
            }
        });
    }
}
