package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.util.function.MapFunctions;
import com.atlassian.labs.remoteapps.util.http.AuthorizationGenerator;
import com.atlassian.labs.remoteapps.util.http.CachingHttpContentRetriever;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.labs.remoteapps.util.uri.UriBuilder;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.singletonList;

@Component
public class RemoteAppAccessorFactory
{

    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final CachingHttpContentRetriever httpContentRetriever;
    private final PluginAccessor pluginAccessor;
    private final UBDispatchFilter ubDispatchFilter;

    @Autowired
    public RemoteAppAccessorFactory(ApplicationLinkAccessor applicationLinkAccessor,
            OAuthLinkManager oAuthLinkManager,
            CachingHttpContentRetriever httpContentRetriever, PluginAccessor pluginAccessor,
            UBDispatchFilter ubDispatchFilter)
    {
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.httpContentRetriever = httpContentRetriever;
        this.pluginAccessor = pluginAccessor;
        this.ubDispatchFilter = ubDispatchFilter;
    }

    public RemoteAppAccessor create(String pluginKey)
    {
        return create(pluginKey, applicationLinkAccessor.getApplicationTypeIfFound(pluginKey));
    }

    public RemoteAppAccessor create(String pluginKey, RemoteAppApplicationType applicationType)
    {
        Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        URI dummyUri = URI.create("http://localhost");
        ServiceProvider dummyProvider = new ServiceProvider(dummyUri, dummyUri, dummyUri);

        if (applicationType != null)
        {
            URI displayUrl = applicationType.getDefaultDetails().getDisplayUrl();
            // don't need to get the actual provider as it doesn't really matter
            return new OAuthSigningRemoteAppAccessor(pluginKey, plugin.getName(), displayUrl, dummyProvider);
        }
        else
        {
            return new OAuthSigningRemoteAppAccessor(
                    pluginKey,
                    plugin.getName(),
                    URI.create(ubDispatchFilter.getLocalMountBaseUrl(pluginKey)),
                    dummyProvider
            );
        }
    }

    private class OAuthSigningRemoteAppAccessor implements RemoteAppAccessor
    {
        private final String key;
        private final String name;
        private final URI displayUrl;
        private final ServiceProvider serviceProvider;

        private OAuthSigningRemoteAppAccessor(String key, String name, URI displayUrl,
                ServiceProvider serviceProvider)
        {
            this.key = key;
            this.name = name;
            this.displayUrl = displayUrl;
            this.serviceProvider = serviceProvider;
        }

        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public URI getDisplayUrl()
        {
            return displayUrl;
        }

        @Override
        public String signGetUrl(String targetPath, Map<String, String[]> params)
        {
            return signGetUrlForType(serviceProvider, getTargetUrl(displayUrl, targetPath), params);
        }

        @Override
        public String createGetUrl(String targetPath, Map<String, String[]> params)
        {
            return executeCreateGetUrl(getTargetUrl(displayUrl, targetPath), params);
        }

        @Override
        public Future<String> executeAsyncGet(String username, String path, Map<String, String> params,
                Map<String, String> headers, HttpContentHandler handler)
                throws ContentRetrievalException
        {
            return executeAsyncGetForType(new OAuthAuthorizationGenerator(serviceProvider), username, path, params, headers, handler);
        }

        @Override
        public AuthorizationGenerator getAuthorizationGenerator()
        {
            return new OAuthAuthorizationGenerator(serviceProvider);
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    private String executeCreateGetUrl(String targetUrl, Map<String, String[]> params)
    {
        return new UriBuilder(Uri.parse(targetUrl)).addQueryParameters(transformValues(params, MapFunctions.STRING_ARRAY_TO_STRING)).toString();
    }

    private Future<String> executeAsyncGetForType(AuthorizationGenerator authorizationGenerator, String targetUrl, String username,
            Map<String, String> params, Map<String, String> headers, HttpContentHandler httpContentHandler)
    {
        return httpContentRetriever.getAsync(authorizationGenerator, username, targetUrl,
                Maps.transformValues(params, MapFunctions.OBJECT_TO_STRING),
                headers, httpContentHandler);
    }

    private String signGetUrlForType(ServiceProvider serviceProvider, String targetUrl, Map<String, String[]> params) throws PermissionDeniedException
    {
        final UriBuilder uriBuilder = new UriBuilder(Uri.parse(targetUrl));

        // adding all the parameters of the signed request
        for (Map.Entry<String, String> param : signRequest(serviceProvider, targetUrl, params, HttpMethod.GET))
        {
            final String value = param.getValue() == null ? "" : param.getValue();
            uriBuilder.addQueryParameter(param.getKey(), value);
        }
        return uriBuilder.toString();
    }

    private String getTargetUrl(URI displayUrl, String targetPath)
    {
        return displayUrl + targetPath;
    }

    private List<Map.Entry<String, String>> signRequest(ServiceProvider serviceProvider,
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

        return oAuthLinkManager.signAsParameters(serviceProvider, method, url, params);
    }

    private class OAuthAuthorizationGenerator implements AuthorizationGenerator
    {
        private final ServiceProvider serviceProvider;

        private OAuthAuthorizationGenerator(ServiceProvider serviceProvider)
        {
            this.serviceProvider = serviceProvider;
        }

        @Override
        public String generate(String method, String url, Map<String, List<String>> parameters)
        {
            return oAuthLinkManager.generateAuthorizationHeader(method, serviceProvider, url,
                    parameters);
        }
    }
}
