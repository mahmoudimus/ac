package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBean;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.RpcEncodedSoapApiScopeHelper;

import java.util.ArrayList;
import java.util.Collection;

public class AddOnScopeApiPathBuilder
{
    Collection<RestApiScopeHelper.RestScope> restResources = new ArrayList<RestApiScopeHelper.RestScope>();
    Collection<RpcEncodedSoapApiScopeHelper> soapResources = new ArrayList<RpcEncodedSoapApiScopeHelper>();

    public AddOnScopeApiPathBuilder withRestPaths(AddOnScopeBean.RestPathBean restPathBean, Collection<String> methods)
    {
        for (String basePath : restPathBean.getBasePaths())
        {
            restResources.add(new RestApiScopeHelper.RestScope(restPathBean.getName(), restPathBean.getVersions(), basePath, methods, true));
        }

        return this;
    }

    public AddOnScopeApiPathBuilder withSoapRpcResources(AddOnScopeBean.SoapRpcPathBean soapRpcPathBean)
    {
        soapResources.add(new RpcEncodedSoapApiScopeHelper("/rpc/soap" + prefixWithSlash(soapRpcPathBean.getPath()), "http://soap.rpc.jira.atlassian.com", soapRpcPathBean.getRpcMethods()));
        return this;
    }

    public AddOnScopeApiPathBuilder withPaths(Iterable<AddOnScopeApiPath> paths)
    {
        for (AddOnScopeApiPath path : paths)
        {
            path.addTo(restResources, soapResources);
        }

        return this;
    }

    public Collection<AddOnScopeApiPath> build()
    {
        Collection<AddOnScopeApiPath> paths = new ArrayList<AddOnScopeApiPath>();

        if (!restResources.isEmpty())
        {
            paths.add(new AddOnScopeApiPath.RestApiPath(restResources));
        }

        if (!soapResources.isEmpty())
        {
            paths.add(new AddOnScopeApiPath.SoapRpcApiPath(soapResources));
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
}
