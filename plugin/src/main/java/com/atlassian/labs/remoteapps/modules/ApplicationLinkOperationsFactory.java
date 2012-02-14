package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.*;
import com.atlassian.labs.remoteapps.util.http.CachingHttpContentRetriever;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.ServletUtils.encodeGetUrl;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.singletonList;

/**
 */
@Component
public class ApplicationLinkOperationsFactory
{

    public static final Function<Object,String> MAP_TO_PARAMS = new Function<Object, String>()
    {
        @Override
        public String apply(Object from)
        {
            return from != null ? from.toString() : null;
        }
    };
    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager oAuthLinkManager;
    private final CachingHttpContentRetriever httpContentRetriever;

    public static interface LinkOperations
    {
        ApplicationLink get();
        String signGetUrl(String user, String targetPath, Map<String, String[]> params);
        String executeGet(String user, String path, Map<String,Object> params) throws ContentRetrievalException;
        void executeGetAsync(String user, String path, Map<String,Object> params, HttpContentHandler handler);
    }

    @Autowired
    public ApplicationLinkOperationsFactory(ApplicationLinkService applicationLinkService, OAuthLinkManager oAuthLinkManager,
                                            CachingHttpContentRetriever httpContentRetriever)
    {
        this.applicationLinkService = applicationLinkService;
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
                    link = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
                }
                return link;
            }

            @Override
            public String signGetUrl(String user, String targetPath, Map<String, String[]> params)
            {
                return signGetUrlForType(get(), user, targetPath, params);
            }

            @Override
            public String executeGet(String username, String path, Map<String, Object> params) throws ContentRetrievalException
            {
                return executeGetForType(get(), username, path, params);
            }

            @Override
            public void executeGetAsync(String username, String path, Map<String, Object> params,
                                        HttpContentHandler handler)
            {
                executeAsyncGetForType(get(), username, path, params, handler);
            }
        };
    }

    private String executeGetForType(ApplicationLink applicationLink, String username, String path, Map<String, Object> params) throws ContentRetrievalException
    {
        String targetUrl = getTargetUrl(applicationLink, path);
        return httpContentRetriever.get(applicationLink, username, targetUrl, Maps.transformValues(params,
                                                                                                   MAP_TO_PARAMS));
    }

    private void executeAsyncGetForType(ApplicationLink applicationLink, String username, String path, Map<String, Object> params,
                                        HttpContentHandler httpContentHandler)
    {
        String targetUrl = getTargetUrl(applicationLink, path);
        httpContentRetriever.getAsync(applicationLink, username, targetUrl,
                                      Maps.transformValues(params, MAP_TO_PARAMS), httpContentHandler);
    }

    private String signGetUrlForType(ApplicationLink applicationLink,
                                     String user,
                                     String targetPath,
                                     Map<String, String[]> params
    ) throws PermissionDeniedException
    {
        String targetUrl = getTargetUrl(applicationLink, targetPath);
        List<Map.Entry<String, String>> message = signRequest(applicationLink, targetUrl, params, HttpMethod.GET);

        return encodeGetUrl(targetUrl, message);
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
