package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraReadUsersAndGroupsScope implements ApiScope
{
    private final RpcEncodedSoapApiScope soapScope = new RpcEncodedSoapApiScope("/rpc/soap/jirasoapservice-v2", asList(
        new RpcEncodedSoapApiScope.SoapScope("http://soap.rpc.jira.atlassian.com", "getUser"),
        new RpcEncodedSoapApiScope.SoapScope("http://soap.rpc.jira.atlassian.com", "getGroup")
    ));

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return soapScope.allow(request);
    }
}
