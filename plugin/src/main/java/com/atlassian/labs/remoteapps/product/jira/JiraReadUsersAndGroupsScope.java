package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraReadUsersAndGroupsScope implements ApiScope
{
    private final Collection<String> methods = asList(
            "getUser",
            "getGroup"
    );
    private final RpcEncodedSoapApiScope soapScope = new RpcEncodedSoapApiScope("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com", methods);
    private final JsonRpcApiScope jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/jirasoapservice-v2", methods);

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return soapScope.allow(request) || jsonrpcScope.allow(request);
    }
}
