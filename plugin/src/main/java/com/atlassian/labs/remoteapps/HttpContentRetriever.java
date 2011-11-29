package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 *
 */
@Component
public class HttpContentRetriever
{
    private final CachingHttpClient httpClient;
    private final OAuthLinkManager oAuthLinkManager;
    private final UserManager userManager;

    @Autowired
    public HttpContentRetriever(OAuthLinkManager oAuthLinkManager, UserManager userManager)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.userManager = userManager;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setMaxObjectSizeBytes(8192);

        httpClient = new CachingHttpClient(new DefaultHttpClient(), cacheConfig);
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
            String authorizationHeader = oAuthLinkManager.signAsHeader(link, HttpMethod.GET, url, Maps.transformValues(parameters, new Function<String, List<String>>()
            {
                @Override
                public List<String> apply(String from)
                {
                    return singletonList(from);
                }
            }));
            httpget.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);

            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
            HttpConnectionParams.setSoTimeout(params, 5 * 1000);
            response = httpClient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }
        catch (IOException e)
        {
            throw new ContentRetrievalException(e);
        }

    }
}
