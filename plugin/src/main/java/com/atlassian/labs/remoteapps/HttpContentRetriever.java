package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

/**
 *
 */
@Component
public class HttpContentRetriever implements DisposableBean
{
    private final CachingHttpClient httpClient;
    private final OAuthLinkManager oAuthLinkManager;
    private final UserManager userManager;

    @Autowired
    public HttpContentRetriever(OAuthLinkManager oAuthLinkManager, UserManager userManager, PluginRetrievalService pluginRetrievalService)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.userManager = userManager;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setMaxObjectSizeBytes(8192);

        DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(
                SchemeRegistryFactory.createDefault(), 2, TimeUnit.SECONDS
        )
        {
            @Override
            protected void finalize() throws Throwable
            {   
                // prevent the ThreadSafeClientConnManager from logging - this causes exceptions due to
                // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                // is still active.
            }
        });
        HttpProtocolParams.setUserAgent(client.getParams(), "Atlassian-RemoteApps/" + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());
        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
            client.getConnectionManager().getSchemeRegistry(),
            ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);
        httpClient = new CachingHttpClient(client, cacheConfig);
    }

    public String get(ApplicationLink link, String url, Map<String,String> parameters) throws ContentRetrievalException
    {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        for (String key : parameters.keySet())
        {
            qparams.add(new BasicNameValuePair(key, parameters.get(key)));
        }
        qparams.add(new BasicNameValuePair("user_id", userManager.getRemoteUsername()));
        HttpGet httpget = new HttpGet(url + "?" + URLEncodedUtils.format(qparams, "UTF-8"));
        HttpContext localContext = new BasicHttpContext();
        HttpResponse response = null;
        try
        {
            oAuthLinkManager.sign(httpget, link, url,
                    Maps.transformValues(parameters, new Function<String, List<String>>()
                    {
                        @Override
                        public List<String> apply(String from)
                        {
                            return singletonList(from);
                        }
                    }));

            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 3 * 1000);
            HttpConnectionParams.setSoTimeout(params, 10 * 1000);
            response = httpClient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200)
            {
                EntityUtils.consume(entity);
                throw new ContentRetrievalException("Unable to retrieve content: " + response.getStatusLine().getReasonPhrase());
            }

            return EntityUtils.toString(entity);
        }
        catch (IOException e)
        {
            throw new ContentRetrievalException(e);
        }
    }

    @Override
    public void destroy() throws Exception
    {
        httpClient.getConnectionManager().shutdown();
    }
}
