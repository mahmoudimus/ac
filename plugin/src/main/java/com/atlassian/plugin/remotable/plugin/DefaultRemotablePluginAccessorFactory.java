package com.atlassian.plugin.remotable.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.remotable.plugin.loader.universalbinary.UBDispatchFilter;
import com.atlassian.plugin.remotable.plugin.module.applinks.RemotePluginContainerModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.util.function.MapFunctions;
import com.atlassian.plugin.remotable.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.remotable.plugin.util.http.ContentRetrievalException;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.RemotablePluginAccessor;
import com.atlassian.plugin.remotable.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.remotable.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.remotable.spi.http.AuthorizationGenerator;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static java.util.Collections.singletonList;

@Component
public final class DefaultRemotablePluginAccessorFactory implements RemotablePluginAccessorFactory, DisposableBean
{
    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final CachingHttpContentRetriever httpContentRetriever;
    private final PluginAccessor pluginAccessor;
    private final UBDispatchFilter ubDispatchFilter;
    private final EventPublisher eventPublisher;

    private final Map<String,RemotablePluginAccessor> accessors;

    @Autowired
    public DefaultRemotablePluginAccessorFactory(ApplicationLinkAccessor applicationLinkAccessor,
                                                 OAuthLinkManager oAuthLinkManager,
                                                 CachingHttpContentRetriever httpContentRetriever,
                                                 PluginAccessor pluginAccessor,
                                                 UBDispatchFilter ubDispatchFilter,
                                                 EventPublisher eventPublisher
    )
    {
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.httpContentRetriever = httpContentRetriever;
        this.pluginAccessor = pluginAccessor;
        this.ubDispatchFilter = ubDispatchFilter;
        this.eventPublisher = eventPublisher;
        this.eventPublisher.register(this);

        this.accessors = CopyOnWriteMap.newHashMap();
    }

    /**
     * Clear accessor if a new application link is discovered
     * @param event
     */
    @EventListener
    public void onApplicationLinkCreated(ApplicationLinkAddedEvent event)
    {
        if (event.getApplicationType() instanceof RemotePluginContainerApplicationType)
        {
            // we clear the whole cache because the plugin key isn't set as a property yet and there
            // is no event for that action
            accessors.clear();
        }
    }

    /**
     * Clear accessor if a application link is deleted
     * @param event
     */
    @EventListener
    public void onApplicationLinkRemoved(ApplicationLinkDeletedEvent event)
    {
        if (event.getApplicationType() instanceof RemotePluginContainerApplicationType)
        {
            accessors.remove((String) event.getApplicationLink().getProperty(RemotePluginContainerModuleDescriptor.PLUGIN_KEY_PROPERTY));
        }
    }

    /**
     * Supplies an accessor for remote plugin operations.  Instances are only meant to be used for the
     * current operation and should not be cached across operations.
     *
     * @param pluginKey The plugin key
     * @return An accessor for either local or remote plugin operations
     */
    public RemotablePluginAccessor get(String pluginKey)
    {
        // this will potentially create multiple instances if called quickly, but we don't really
        // care as they shouldn't be cached
        if (accessors.containsKey(pluginKey))
        {
            return accessors.get(pluginKey);
        }
        else
        {
            ApplicationLink link = applicationLinkAccessor.getApplicationLink(pluginKey);
            return create(pluginKey,
                    link != null ? link.getDisplayUrl() : URI.create(ubDispatchFilter.getLocalMountBaseUrl(pluginKey)));
        }

    }

    /**
     * Supplies an accessor for remote plugin operations but always creates a new one.  Instances are
     * still only meant to be used for the current operation and should not be cached across
     * operations.
     *
     * This method is useful for when the display url is known but the application link has not yet
     * been created
     * @param pluginKey The plugin key
     * @param displayUrl The display url
     * @return An accessor for a remote plugin
     */
    public RemotablePluginAccessor create(String pluginKey, URI displayUrl)
    {
        Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        checkNotNull(plugin, "Plugin not found: {}", pluginKey);
        URI dummyUri = URI.create("http://localhost");
        ServiceProvider dummyProvider = new ServiceProvider(dummyUri, dummyUri, dummyUri);

        // don't need to get the actual provider as it doesn't really matter
        OAuthSigningRemotablePluginAccessor oAuthSigningRemotablePluginAccessor = new OAuthSigningRemotablePluginAccessor(
                pluginKey, plugin.getName(), displayUrl, dummyProvider);
        accessors.put(pluginKey, oAuthSigningRemotablePluginAccessor);
        return oAuthSigningRemotablePluginAccessor;
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    private class OAuthSigningRemotablePluginAccessor implements RemotablePluginAccessor
    {
        private final String key;
        private final String name;
        private final URI displayUrl;
        private final ServiceProvider serviceProvider;

        private OAuthSigningRemotablePluginAccessor(String key,
                                                    String name,
                                                    URI displayUrl,
                                                    ServiceProvider serviceProvider
        )
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
        public String signGetUrl(URI targetPath, Map<String, String[]> params)
        {
            return signGetUrlForType(serviceProvider, getTargetUrl(displayUrl, targetPath), params);
        }

        @Override
        public String createGetUrl(URI targetPath, Map<String, String[]> params)
        {
            return executeCreateGetUrl(getTargetUrl(displayUrl, targetPath), params);
        }

        @Override
        public Promise<String> executeAsyncGet(String username, URI path, Map<String, String> params,
                Map<String, String> headers)
                throws ContentRetrievalException
        {
            return executeAsyncGetForType(new OAuthAuthorizationGenerator(serviceProvider),
                    getTargetUrl(displayUrl, path), username, params, headers, key);
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

    private String executeCreateGetUrl(URI targetUrl, Map<String, String[]> params)
    {
        return new UriBuilder(Uri.fromJavaUri(targetUrl)).addQueryParameters(transformValues(params, MapFunctions.STRING_ARRAY_TO_STRING)).toString();
    }

    private Promise<String> executeAsyncGetForType(AuthorizationGenerator authorizationGenerator, URI targetUrl, String username,
            Map<String, String> params, Map<String, String> headers, String pluginKey)
    {
        return httpContentRetriever.getAsync(authorizationGenerator, username, targetUrl,
            Maps.transformValues(params, MapFunctions.OBJECT_TO_STRING),
            headers, pluginKey);
    }

    private String signGetUrlForType(ServiceProvider serviceProvider, URI targetUrl,
            Map<String, String[]> params) throws PermissionDeniedException
    {
        final UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(targetUrl));

        // adding all the parameters of the signed request
        for (Map.Entry<String, String> param : signRequest(serviceProvider, targetUrl, params, HttpMethod.GET))
        {
            final String value = param.getValue() == null ? "" : param.getValue();
            uriBuilder.addQueryParameter(param.getKey(), value);
        }
        return uriBuilder.toString();
    }

    private URI getTargetUrl(URI displayUrl, URI targetPath)
    {
        return URI.create(displayUrl.toString() + targetPath.getPath());
    }

    private List<Map.Entry<String, String>> signRequest(ServiceProvider serviceProvider,
            URI url,
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
        public String generate(String method, URI url, Map<String, List<String>> parameters)
        {
            return oAuthLinkManager.generateAuthorizationHeader(method, serviceProvider, url,
                    parameters);
        }
    }
}
