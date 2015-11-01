package com.atlassian.plugin.connect.test.client;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;

public class UserRequestSender
{
    private final String baseUrl;

    public UserRequestSender(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public <T> T sendRequestAsUser(HttpRequest request, ResponseHandler<T> handler, String username, String password) throws Exception
    {
        URI uri = new java.net.URI(baseUrl);

        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

        return getDefaultHttpClient(username, password).execute(targetHost, request, handler, localcontext);
    }

    public DefaultHttpClient getDefaultHttpClient(String username, String password)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicClientConnectionManager());
        httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return httpclient;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }
}
