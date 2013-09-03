package com.atlassian.connect.capabilities.client;

import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.springframework.stereotype.Component;

@Component
public class DefaultHttpClientFactory implements HttpClientFactory
{
    private static final int SOCKET_TIMEOUT = Integer.getInteger("capabilities.httpclient.sotimeout", 15000);
    private static final int CONNECTION_TIMEOUT = Integer.getInteger("capabilities.httpclient.conntimeout", 3000);
    private static final int CONNECTION_POOL_TIMEOUT_IN_MILLIS = Integer.getInteger("capabilities.httpclient.pool.timeout", 3600000);
    
    @Override
    public HttpClient createHttpClient()
    {
        final DefaultHttpClient defaultHttpClient = new DefaultHttpClient(createClientConnectionManager(), createParams());
        defaultHttpClient.setRoutePlanner(createRoutePlaner());
        defaultHttpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
       
        return defaultHttpClient;
    }

    private ClientConnectionManager createClientConnectionManager()
    {
        return new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), CONNECTION_POOL_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    private HttpParams createParams()
    {
        HttpParams params = new SyncBasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        return params;
    }

    private HttpRoutePlanner createRoutePlaner()
    {
        return new ProxySelectorRoutePlanner(SchemeRegistryFactory.createDefault(), ProxySelector.getDefault());
    }
}
