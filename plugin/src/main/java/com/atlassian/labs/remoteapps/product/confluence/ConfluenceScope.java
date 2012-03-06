package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScopeHelper;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScopeHelper;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.collect.Iterables.concat;

/**
 *
 */
abstract class ConfluenceScope implements ApiScope
{
    private final XmlRpcApiScopeHelper v2XmlRpcApiScopeHelper;
    private final XmlRpcApiScopeHelper v1XmlRpcApiScopeHelper;
    private final JsonRpcApiScopeHelper v2JsonRpcScopeHelper;
    private final JsonRpcApiScopeHelper v1JsonRpcScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    protected ConfluenceScope(Collection<String> methods)
    {
        v1JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v1", methods);
        v2JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v2", methods);
        v1XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence1")));
        v2XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence2")));
        this.apiResourceInfo = concat(v1JsonRpcScopeHelper.getApiResourceInfos(), v2JsonRpcScopeHelper.getApiResourceInfos(), v1XmlRpcApiScopeHelper.getApiResourceInfos(), v2XmlRpcApiScopeHelper.getApiResourceInfos());
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return v1XmlRpcApiScopeHelper.allow(request, user) || v2XmlRpcApiScopeHelper.allow(request, user) || v1JsonRpcScopeHelper.allow(request, user) || v2JsonRpcScopeHelper.allow(request, user);
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
