package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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

    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager oAuthLinkManager;
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private final HttpContentRetriever httpContentRetriever;

    public static interface LinkOperations
    {
        ApplicationLink get();
        boolean canAccess(String user);
        String signGetUrl(String user, String targetPath, Map<String, String[]> params);
        String executeGet(String user, String path, Map<String,Object> params) throws ContentRetrievalException;
    }

    @Autowired
    public ApplicationLinkOperationsFactory(ApplicationLinkService applicationLinkService, OAuthLinkManager oAuthLinkManager,
                                            PermissionManager permissionManager, UserManager userManager, HttpContentRetriever httpContentRetriever)
    {
        this.applicationLinkService = applicationLinkService;
        this.oAuthLinkManager = oAuthLinkManager;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
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
            public boolean canAccess(String user)
            {
                return permissionManager.canAccessRemoteApp(user, get());
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
        };
    }

    private String executeGetForType(ApplicationLink applicationLink, String username, String path, Map<String, Object> params) throws ContentRetrievalException
    {
        String targetUrl = getTargetUrl(applicationLink, path);
        return httpContentRetriever.get(applicationLink, username, targetUrl, Maps.transformValues(params, new Function<Object, String>() {

            @Override
            public String apply(Object from)
            {
                return from != null ? from.toString() : null;
            }
        }));
    }

    private String signGetUrlForType(ApplicationLink applicationLink,
                                     String user,
                                     String targetPath,
                                     Map<String, String[]> params
    ) throws PermissionDeniedException
    {
        if (!permissionManager.canAccessRemoteApp(user, applicationLink))
        {
            throw new PermissionDeniedException("User not authorized");
        }
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
