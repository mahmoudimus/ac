package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * This is a public component
 */
public class CachingHttpContentRetriever implements DisposableBean, HttpContentRetriever
{
    private final FlushableHttpCacheStorage httpCacheStorage;
    CachingHttpClient httpClient;
    private final OAuthLinkManager oAuthLinkManager;
    private final UserManager userManager;
    private final Logger log = LoggerFactory.getLogger(CachingHttpContentRetriever.class);

    // fixme: change to a bounded queue
    private final Executor asyncRequestExecutor = Executors.newSingleThreadExecutor();

    public CachingHttpContentRetriever(OAuthLinkManager oAuthLinkManager, UserManager userManager,
                                       PluginRetrievalService pluginRetrievalService)
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
        HttpParams params = client.getParams();
        HttpProtocolParams.setUserAgent(params, "Atlassian-RemoteApps/" + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());
        HttpConnectionParams.setConnectionTimeout(params, 3 * 1000);
        HttpConnectionParams.setSoTimeout(params, 10 * 1000);


        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
            client.getConnectionManager().getSchemeRegistry(),
            ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);

        httpCacheStorage = new FlushableHttpCacheStorage(cacheConfig);
        httpClient = new CachingHttpClient(client, httpCacheStorage, cacheConfig);
    }

    @Override
    public void flushCacheByUrlPattern(Pattern urlPattern)
    {
        httpCacheStorage.flushByUrlPattern(urlPattern);
    }

    @Override
    public void getAsync(final ApplicationLink link, final String remoteUsername, final String url,
                         final Map<String, String> parameters,
                         final HttpContentHandler handler)
    {
        asyncRequestExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String content = get(link, remoteUsername, url, parameters);
                    handler.onSuccess(content);
                }
                catch (ContentRetrievalException ex)
                {
                    handler.onError(ex);
                }
            }
        });
    }
    
    @Override
    public String get(ApplicationLink link, String remoteUsername, String originalUrl, Map<String, String> parameters) throws
                                                                                                              ContentRetrievalException
    {
        String url = getUrlWithUserId(originalUrl, remoteUsername, parameters);
        HttpGet httpget = new HttpGet(url);
        HttpContext localContext = new BasicHttpContext();
        HttpResponse response = null;
        try
        {
            oAuthLinkManager.sign(httpget, link, originalUrl, remoteUsername,
                    Maps.transformValues(parameters, new Function<String, List<String>>()
                    {
                        @Override
                        public List<String> apply(String from)
                        {
                            return singletonList(from);
                        }
                    }));


            log.info("Retrieving content from remote app '{}' on URL '{}' for user '{}'", new Object[]{link.getId().get(), url, remoteUsername});
            response = httpClient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200)
            {
                EntityUtils.consume(entity);
                throw new ContentRetrievalException(response.getStatusLine().getReasonPhrase());
            }

            return EntityUtils.toString(entity);
        }
        catch (IOException e)
        {
            log.warn("Unable to retrieve information from '{}' as user '{}' due to: {}", new Object[]{url, remoteUsername, e.getMessage()});
            throw new ContentRetrievalException(e);
        }
    }

    private String getUrlWithUserId(String url, String remoteUsername)
    {
        return getUrlWithUserId(url, remoteUsername, Collections.<String,String>emptyMap());
    }
    private String getUrlWithUserId(String url, String remoteUsername,
            Map<String, String> parameters)
    {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        for (String key : parameters.keySet())
        {
            qparams.add(new BasicNameValuePair(key, parameters.get(key)));
        }
        if (remoteUsername != null)
        {
            qparams.add(new BasicNameValuePair("user_id", remoteUsername));
        }
        String queryString = URLEncodedUtils.format(qparams, "UTF-8");
        if (!isEmpty(queryString))
        {
            url += "?" + queryString;
        }
        return url;
    }

    @Override
    public void destroy() throws Exception
    {
        httpClient.getConnectionManager().shutdown();
    }

    @Override
    public void postIgnoreResponse(ApplicationLink link, String url, String jsonBody)
    {
        String user = userManager.getRemoteUsername();
        HttpPost httpPost = new HttpPost(getUrlWithUserId(url, user));
        HttpResponse response = null;
        try
        {
            oAuthLinkManager.sign(httpPost, link, url, user, Collections.<String, List<String>>emptyMap());
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setEntity(new StringEntity(jsonBody));
            log.info("Posting information to '{}' as user '{}'", url, user);
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 200)
            {
                log.error("Unable to post to " + url + " due to " + response.getStatusLine().toString());
            }
            EntityUtils.consume(response.getEntity());
        }
        catch (IOException e)
        {
            log.warn("Unable to post information to '{}' as user '{}' due to: {}", new Object
                    []{url, user, e.getMessage()});
            throw new ContentRetrievalException(e);
        }
    }
}
