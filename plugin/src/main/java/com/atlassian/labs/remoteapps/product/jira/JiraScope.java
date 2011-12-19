package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 *
 */
class JiraScope implements ApiScope
{
    private final RpcEncodedSoapApiScope soapScope;
    private final JsonRpcApiScope jsonrpcScope;

    protected JiraScope(Collection<String> methods)
    {
        soapScope = new RpcEncodedSoapApiScope("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com", methods);
        jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/jirasoapservice-v2", methods);
    }
    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return soapScope.allow(request) || jsonrpcScope.allow(request);
    }
}
