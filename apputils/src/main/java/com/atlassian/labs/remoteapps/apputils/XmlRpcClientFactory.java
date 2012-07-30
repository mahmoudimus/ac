package com.atlassian.labs.remoteapps.apputils;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import redstone.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;

/**
 * Helps make authenticated xmlrpc calls
 */
public class XmlRpcClientFactory
{
    private final SignedRequestHandler signedRequestHandler;

    public XmlRpcClientFactory(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    public XmlRpcClient create(String appKey, String userId) throws MalformedURLException
    {
        // todo: fix this but in doing so, make sure it works in the same app that needs to pass base url
        String baseUrl = appKey.startsWith("http") ? appKey : signedRequestHandler.getHostBaseUrl(appKey);
        final String url = baseUrl + "/rpc/xmlrpc";
        XmlRpcClient client = new XmlRpcClient(url + "?user_id=" + userId, false);
        client.setRequestProperty("Authorization", signedRequestHandler.getAuthorizationHeaderValue(
                url, "POST", userId));
        return client;
    }
}
