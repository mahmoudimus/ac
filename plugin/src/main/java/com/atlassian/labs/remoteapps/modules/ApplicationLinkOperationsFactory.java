package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
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
    private final ApplicationProperties applicationProperties;
    private final HttpContentRetriever httpContentRetriever;

    public static interface LinkOperations
    {
        ApplicationLink get();
        boolean canAccess(String user);
        String signGetUrl(HttpServletRequest req, String targetPath);
        String signGetUrl(String user, String targetPath);
        String executeGet(String path, Map<String,Object> params) throws ContentRetrievalException;
    }

    @Autowired
    public ApplicationLinkOperationsFactory(ApplicationLinkService applicationLinkService, OAuthLinkManager oAuthLinkManager, PermissionManager permissionManager, UserManager userManager, ApplicationProperties applicationProperties, HttpContentRetriever httpContentRetriever)
    {
        this.applicationLinkService = applicationLinkService;
        this.oAuthLinkManager = oAuthLinkManager;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
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
            public String signGetUrl(HttpServletRequest req, String targetPath)
            {
                return signGetUrlForType(get(), userManager.getRemoteUsername(req), targetPath, req.getParameterMap());
            }

            @Override
            public String signGetUrl(String user, String targetPath)
            {
                return signGetUrlForType(get(), user, targetPath, Collections.<String, String[]>emptyMap());
            }

            @Override
            public String executeGet(String path, Map<String, Object> params) throws ContentRetrievalException
            {
                return executeGetForType(get(), path, params);
            }
        };
    }

    private String executeGetForType(ApplicationLink applicationLink, String path, Map<String, Object> params) throws ContentRetrievalException
    {
        String targetUrl = getTargetUrl(applicationLink, path);
        return httpContentRetriever.get(applicationLink, targetUrl, Maps.transformValues(params, new Function<Object, String>() {

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
        return applicationLink.getRpcUrl() + targetPath;
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
        URI hostUri = URI.create(applicationProperties.getBaseUrl());
        String host = hostUri.getScheme() + "://" + hostUri.getHost() + ":" + hostUri.getPort();

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
        params.put("xdm_e", singletonList(host));
        params.put("xdm_c", singletonList("channel01"));
        params.put("xdm_p", singletonList("1"));

        return oAuthLinkManager.signAsParameters(applicationLink, method, url, params);
    }
}
