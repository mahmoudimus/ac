package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

/**
 *
 */
class ConfluenceScope implements ApiScope
{
    private final XmlRpcApiScope xmlRpcApiScope;
    private final JsonRpcApiScope jsonrpcScope;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    protected ConfluenceScope(Collection<String> methods)
    {
        jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/confluenceservice-v2", methods);
        xmlRpcApiScope = new XmlRpcApiScope("/rpc/xmlrpc", Collections2.transform(methods, new Function<String, String>()
        {
            @Override
            public String apply(String from)
            {
                return "confluence2." + from;
            }
        }));
        this.apiResourceInfo = concat(jsonrpcScope.getApiResourceInfos(), xmlRpcApiScope.getApiResourceInfos());
    }
    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return xmlRpcApiScope.allow(request, user) || jsonrpcScope.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
