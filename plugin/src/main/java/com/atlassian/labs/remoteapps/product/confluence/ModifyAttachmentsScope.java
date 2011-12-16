package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static java.util.Arrays.asList;

/**
 *
 */
public class ModifyAttachmentsScope implements ApiScope
{
    private final XmlRpcApiScope xmlrpcScope = new XmlRpcApiScope("/rpc/xmlrpc", methodList("confluence2."));

    private final JsonRpcApiScope jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/confluenceservice-v2",
            methodList(""));

    private final List<String> methodList(String prefix)
    {
        return asList(
                prefix + "addAttachment",
                prefix + "removeAttachment",
                prefix + "moveAttachment"
        );
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return xmlrpcScope.allow(request) || jsonrpcScope.allow(request);
    }
}
