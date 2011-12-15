package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadUsersAndGroupsScope implements ApiScope
{
    private final XmlRpcApiScope xmlrpcScope = new XmlRpcApiScope("/rpc/xmlrpc", asList(
            "confluence2.getUser",
            "confluence2.getUserGroups",
            "confluence2.getGroups",
            "confluence2.hasUser",
            "confluence2.hasGroup",
            "confluence2.getActiveUsers",
            "confluence2.getUserInformation"
    ));
    private final JsonRpcApiScope jsonrpcScope = new JsonRpcApiScope("/rpc/xmlrpc", asList(
            "getUser",
            "getUserGroups",
            "getGroups",
            "hasUser",
            "hasGroup",
            "getActiveUsers",
            "getUserInformation"
    ));
    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return xmlrpcScope.allow(request) && jsonrpcScope.allow(request);
    }
}
