package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;

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

    protected JiraScope(Collection<String> methods)
    {
        soapScope = new RpcEncodedSoapApiScope("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com", methods);
        jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/jirasoapservice-v2", methods);
        this.apiResourceInfo = concat(soapScope.getApiResourceInfos(), jsonrpcScope.getApiResourceInfos());
    }
    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return soapScope.allow(request, user) || jsonrpcScope.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
