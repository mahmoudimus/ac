package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.spi.XmlDescriptor;
import com.atlassian.plugin.connect.spi.permission.AbstractPermission;
import com.atlassian.plugin.connect.spi.permission.scope.*;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

@XmlDescriptor
abstract class ConfluenceScope extends AbstractPermission implements ApiScope
{
    private final XmlRpcApiScopeHelper v2XmlRpcApiScopeHelper;
    private final XmlRpcApiScopeHelper v1XmlRpcApiScopeHelper;
    private final JsonRpcApiScopeHelper v2JsonRpcScopeHelper;
    private final JsonRpcApiScopeHelper v1JsonRpcScopeHelper;
    private final RestApiScopeHelper restApiScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    private final PathScopeHelper downloadScopeHelper;

    protected ConfluenceScope(String key, Collection<String> methods)
    {

        this(key, methods, Collections.<RestApiScopeHelper.RestScope>emptyList(), new PathScopeHelper(false));
    }

    protected ConfluenceScope(String key, Collection<String> methods, Collection<RestApiScopeHelper.RestScope> resources)
    {
        this(key, methods, resources, new PathScopeHelper(false));
    }

    protected ConfluenceScope(String key, Collection<String> methods, PathScopeHelper pathScopeHelper)
    {
        this(key, methods, Collections.<RestApiScopeHelper.RestScope>emptyList(), pathScopeHelper);
    }

    protected ConfluenceScope(String key, Collection<String> methods, Collection<RestApiScopeHelper.RestScope> resources, PathScopeHelper pathScopeHelper)
    {
        super(key);

        this.v1JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v1", methods);
        this.v2JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v2", methods);
        this.v1XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence1")));
        this.v2XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence2")));
        this.restApiScopeHelper = new RestApiScopeHelper(checkNotNull(resources));
        this.downloadScopeHelper = checkNotNull(pathScopeHelper);
        this.apiResourceInfo = concat(v1JsonRpcScopeHelper.getApiResourceInfos(),
                v2JsonRpcScopeHelper.getApiResourceInfos(),
                v1XmlRpcApiScopeHelper.getApiResourceInfos(),
                v2XmlRpcApiScopeHelper.getApiResourceInfos(),
                pathScopeHelper.getApiResourceInfos());
    }

    @Override
    public final boolean allow(HttpServletRequest request, UserKey user)
    {
        return v1XmlRpcApiScopeHelper.allow(request, user) ||
                v2XmlRpcApiScopeHelper.allow(request, user) ||
                v1JsonRpcScopeHelper.allow(request, user) ||
                v2JsonRpcScopeHelper.allow(request, user) ||
                restApiScopeHelper.allow(request, user) ||
                downloadScopeHelper.allow(request, user);
    }

    @Override
    public final Iterable<ApiResourceInfo> getApiResourceInfos()
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
