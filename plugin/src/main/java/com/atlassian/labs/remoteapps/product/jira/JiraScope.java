package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.collect.Iterables.concat;

/**
 *
 */
class JiraScope implements ApiScope
{
    private final RpcEncodedSoapApiScope soapScope;
    private final JsonRpcApiScope jsonrpcScope;
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private RestApiScope restApiScope;

    protected JiraScope(Collection<String> methods, Collection<RestApiScope.RestScope> resources)
    {
        soapScope = new RpcEncodedSoapApiScope("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com",
                methods);
        jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/jirasoapservice-v2", methods);
        restApiScope = new RestApiScope(resources);
        this.apiResourceInfo = concat(soapScope.getApiResourceInfos(), jsonrpcScope.getApiResourceInfos(),
                restApiScope.getApiResourceInfos());
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return soapScope.allow(request, user) || jsonrpcScope.allow(request, user) || restApiScope.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
