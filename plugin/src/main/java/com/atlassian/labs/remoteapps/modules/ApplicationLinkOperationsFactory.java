package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.util.function.MapFunctions;
import com.atlassian.labs.remoteapps.util.http.CachingHttpContentRetriever;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.labs.remoteapps.util.uri.UriBuilder;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.singletonList;

@Component
public class ApplicationLinkOperationsFactory
{

    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final CachingHttpContentRetriever httpContentRetriever;

    public static interface LinkOperations
    {
        ApplicationLink get();
        String signGetUrl(String targetPath, Map<String, String[]> params);
        String createGetUrl(String targetPath, Map<String, String[]> params);
        Future<String> executeAsyncGet(String user, String path, Map<String, String> params,
                Map<String, String> headers, HttpContentHandler handler) throws ContentRetrievalException;
    }

    @Autowired
    public ApplicationLinkOperationsFactory(ApplicationLinkAccessor applicationLinkAccessor, OAuthLinkManager oAuthLinkManager,
                                            CachingHttpContentRetriever httpContentRetriever)
    {
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.httpContentRetriever = httpContentRetriever;
    }

    public LinkOperations create(final ApplicationType applicationType)
    {
        return new LinkOperations()
        {
            ApplicationLink link;
            @Override
            public synchronized ApplicationLink get()
            {
                if (link == null)
                {
                    link = applicationLinkAccessor.getApplicationLink(applicationType);
                }
                return link;
            }

            @Override
            public String signGetUrl(String targetPath, Map<String, String[]> params)
            {
                return signGetUrlForType(get(), targetPath, params);
            }

            @Override
            public String createGetUrl(String targetPath, Map<String, String[]> params)
            {
                return executeCreateGetUrl(get(), targetPath, params);
            }

            @Override
            public Future<String> executeAsyncGet(String username, String path, Map<String, String> params,
                    Map<String, String> headers, HttpContentHandler handler)
                    throws ContentRetrievalException
            {
                return executeAsyncGetForType(get(), username, path, params, headers, handler);
            }
        };
    }

    private String executeCreateGetUrl(ApplicationLink applicationLink, String targetPath, Map<String, String[]> params)
    {
        final String targetUrl = getTargetUrl(applicationLink, targetPath);
        return new UriBuilder(Uri.parse(targetUrl)).addQueryParameters(transformValues(params, MapFunctions.STRING_ARRAY_TO_STRING)).toString();
    }

    private Future<String> executeAsyncGetForType(ApplicationLink applicationLink, String username, String path,
            Map<String, String> params, Map<String, String> headers, HttpContentHandler httpContentHandler)
    {
        String targetUrl = getTargetUrl(applicationLink, path);
        return httpContentRetriever.getAsync(applicationLink, username, targetUrl,
                                      Maps.transformValues(params, MapFunctions.OBJECT_TO_STRING),
                                      headers, httpContentHandler);
    }

    private String signGetUrlForType(ApplicationLink applicationLink, String targetPath, Map<String, String[]> params) throws PermissionDeniedException
    {
        final String targetUrl = getTargetUrl(applicationLink, targetPath);
        final UriBuilder uriBuilder = new UriBuilder(Uri.parse(targetUrl));

        // adding all the parameters of the signed request
        for (Map.Entry<String, String> param : signRequest(applicationLink, targetUrl, params, HttpMethod.GET))
        {
            final String value = param.getValue() == null ? "" : param.getValue();
            uriBuilder.addQueryParameter(param.getKey(), value);
        }
        return uriBuilder.toString();
    }

    private String getTargetUrl(ApplicationLink applicationLink, String targetPath)
    {
        return applicationLink.getDisplayUrl() + targetPath;
    }

    private List<Map.Entry<String, String>> signRequest(ApplicationLink applicationLink,
                                                        String url,
                                                        Map<String, String[]> queryParams,
                                                        String method
    )
    {
        String timestamp = System.currentTimeMillis() / 1000 + "";
        String nonce = System.nanoTime() + "";
        String signatureMethod = OAuth.RSA_SHA1;
        String oauthVersion = "1.0";

        Map<String,List<String>> params = newHashMap(transformValues(queryParams, new Function<String[], List<String>>()
        {
            @Override
            public List<String> apply(String[] from)
            {
                return Arrays.asList(from);
            }
        }));

        params.put(OAuth.OAUTH_SIGNATURE_METHOD, singletonList(signatureMethod));
        params.put(OAuth.OAUTH_NONCE, singletonList(nonce));
        params.put(OAuth.OAUTH_VERSION, singletonList(oauthVersion));
        params.put(OAuth.OAUTH_TIMESTAMP, singletonList(timestamp));

        return oAuthLinkManager.signAsParameters(applicationLink, method, url, params);
    }
}
