package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.connect.modules.beans.nested.AddonScopeBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class AddonScopeApiPathBuilder
{
    Collection<RestApiScopeHelper.RestScope> restResources = new ArrayList<>();
    Collection<RpcEncodedSoapApiScopeHelper> soapResources = new ArrayList<>();
    Collection<JsonRpcApiScopeHelper> jsonResources = new ArrayList<>();
    Collection<XmlRpcApiScopeHelper> xmlResources = new ArrayList<>();
    Collection<PathScopeHelper> paths = new ArrayList<>();

    public AddonScopeApiPathBuilder withRestPaths(AddonScopeBean.RestPathBean restPathBean, Collection<String> methods)
    {
        restResources.addAll(restPathBean.getBasePaths()
            .stream()
            .map(basePath -> new RestApiScopeHelper.RestScope(restPathBean.getName(), restPathBean.getVersions(),
                basePath, methods, true))
            .collect(Collectors.toList())
        );

        return this;
    }

    public AddonScopeApiPathBuilder withSoapRpcResources(AddonScopeBean.SoapRpcPathBean soapRpcPathBean, Collection<String> httpMethods)
    {
        for (String path : soapRpcPathBean.getPaths())
        {
            soapResources.addAll(httpMethods.stream()
                .map(httpMethod -> new RpcEncodedSoapApiScopeHelper("/rpc/soap" + prefixWithSlash(path),
                    "http://soap.rpc.jira.atlassian.com", soapRpcPathBean.getRpcMethods(), httpMethod))
                .collect(Collectors.toList())
            );
        }
        return this;
    }

    public AddonScopeApiPathBuilder withJsonRpcResources(AddonScopeBean.JsonRpcPathBean jsonRpcPathBean, Collection<String> httpMethods)
    {
        for (String path : jsonRpcPathBean.getPaths())
        {
            jsonResources.addAll(httpMethods.stream()
                .map(httpMethod -> new JsonRpcApiScopeHelper("/rpc/json-rpc" + prefixWithSlash(path),
                    jsonRpcPathBean.getRpcMethods(), httpMethod))
                .collect(Collectors.toList())
            );
        }
        return this;
    }

    public AddonScopeApiPathBuilder withXmlRpcResources(AddonScopeBean.XmlRpcBean xmlRpcBean)
    {
        xmlResources.addAll(xmlRpcBean.getPrefixes().stream()
            .map(prefix -> new XmlRpcApiScopeHelper("/rpc/xmlrpc", prefixXmlRpcMethods(xmlRpcBean.getRpcMethods(), prefix)))
            .collect(Collectors.toList())
        );
        return this;
    }

    public AddonScopeApiPathBuilder withPaths(AddonScopeBean.PathBean path, Collection<String> httpMethods)
    {
        paths.addAll(httpMethods.stream()
                .map(method -> new PathScopeHelper(true, path.getPaths(), method))
                .collect(Collectors.toList()));
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
        return rpcMethods.stream()
                .map(method -> prefix + "." + method)
                .collect(Collectors.toList());
    }
}
