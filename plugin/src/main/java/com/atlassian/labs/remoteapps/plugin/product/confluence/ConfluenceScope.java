package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.spi.permission.scope.*;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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
    private RestApiScopeHelper restApiScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    protected ConfluenceScope(Collection<String> methods)
    {
        this(methods, Collections.<RestApiScopeHelper.RestScope>emptyList());
    }

    protected ConfluenceScope(Collection<String> methods, Collection<RestApiScopeHelper.RestScope> resources)
    {
        v1JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v1", methods);
        v2JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v2", methods);
        v1XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence1")));
        v2XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence2")));
        restApiScopeHelper = new RestApiScopeHelper(resources);
        this.apiResourceInfo = concat(v1JsonRpcScopeHelper.getApiResourceInfos(), v2JsonRpcScopeHelper.getApiResourceInfos(), v1XmlRpcApiScopeHelper.getApiResourceInfos(), v2XmlRpcApiScopeHelper.getApiResourceInfos());
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return v1XmlRpcApiScopeHelper.allow(request, user) || v2XmlRpcApiScopeHelper.allow(request, user) || v1JsonRpcScopeHelper.allow(request, user) || v2JsonRpcScopeHelper.allow(request, user) || restApiScopeHelper.allow(request, user);
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
