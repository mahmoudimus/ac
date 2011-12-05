package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 *
 */
public class ModifyAttachmentsScope implements ApiScope
{
    private final XmlRpcApiScope xmlrpcScope = new XmlRpcApiScope("/rpc/xmlrpc", asList(
            "confluence2.addAttachment",
            "confluence2.removeAttachment",
            "confluence2.moveAttachment"
    ));
    @Override
    public boolean allow(HttpServletRequest request)
    {
        return xmlrpcScope.allow(request);
    }
}
