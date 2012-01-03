package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.collect.Iterables.concat;

/**
 *
 */
class ConfluenceScope implements ApiScope
{
    private final XmlRpcApiScope v2XmlRpcApiScope;
    private final XmlRpcApiScope v1XmlRpcApiScope;
    private final JsonRpcApiScope v2JsonRpcScope;
    private final JsonRpcApiScope v1JsonRpcScope;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    protected ConfluenceScope(Collection<String> methods)
    {
        v1JsonRpcScope = new JsonRpcApiScope("/rpc/json-rpc/confluenceservice-v1", methods);
        v2JsonRpcScope = new JsonRpcApiScope("/rpc/json-rpc/confluenceservice-v2", methods);
        v1XmlRpcApiScope = new XmlRpcApiScope("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence1")));
        v2XmlRpcApiScope = new XmlRpcApiScope("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence2")));
        this.apiResourceInfo = concat(v1JsonRpcScope.getApiResourceInfos(), v2JsonRpcScope.getApiResourceInfos(), v1XmlRpcApiScope.getApiResourceInfos(), v2XmlRpcApiScope.getApiResourceInfos());
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return v1XmlRpcApiScope.allow(request, user) || v2XmlRpcApiScope.allow(request, user) || v1JsonRpcScope.allow(request, user) || v2JsonRpcScope.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    private Function<String, String> xmlRpcTransform(final String serviceName)
    {
        return new Function<String, String>()
        {

            @Override
            public String apply(String from)
            {
                return serviceName + "." + from;
            }
        };
    }
}
