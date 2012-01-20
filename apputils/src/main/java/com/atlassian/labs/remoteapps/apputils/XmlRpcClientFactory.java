package com.atlassian.labs.remoteapps.apputils;

import redstone.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 21/01/12
 * Time: 1:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class XmlRpcClientFactory
{
    private final OAuthContext oauthContext;

    public XmlRpcClientFactory(OAuthContext oauthContext)
    {
        this.oauthContext = oauthContext;
    }

    public XmlRpcClient create(String consumerKey, String userId) throws MalformedURLException
    {
        final String url = oauthContext.getHostBaseUrl(consumerKey) + "/rpc/xmlrpc";
        XmlRpcClient client = new XmlRpcClient(url + "?user_id=" + userId, false);
        client.setRequestProperty("Authorization", oauthContext.getAuthorizationHeaderValue(
                url, "POST", userId));
        return client;
    }
}
