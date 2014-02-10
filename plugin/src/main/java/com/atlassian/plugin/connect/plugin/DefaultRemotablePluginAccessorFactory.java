package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class DefaultRemotablePluginAccessorFactory implements RemotablePluginAccessorFactory, DisposableBean
{
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final OAuthLinkManager oAuthLinkManager;
    private final CachingHttpContentRetriever httpContentRetriever;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private final JwtService jwtService;
    private final ConsumerService consumerService;
    private final UserManager userManager;

    private final Map<String, RemotablePluginAccessor> accessors;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemotablePluginAccessorFactory.class);

    @Autowired
    public DefaultRemotablePluginAccessorFactory(ConnectApplinkManager connectApplinkManager,
            ConnectAddonRegistry connectAddonRegistry,
            OAuthLinkManager oAuthLinkManager,
            CachingHttpContentRetriever httpContentRetriever,
            PluginAccessor pluginAccessor,
            ApplicationProperties applicationProperties,
            EventPublisher eventPublisher,
            JwtService jwtService,
            ConsumerService consumerService,
            UserManager userManager)
    {
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddonRegistry = connectAddonRegistry;
        this.oAuthLinkManager = oAuthLinkManager;
        this.httpContentRetriever = httpContentRetriever;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.consumerService = consumerService;
        this.userManager = userManager;
        this.eventPublisher.register(this);
        this.jwtService = jwtService;

        this.accessors = CopyOnWriteMap.newHashMap();
    }

    /**
     * Clear accessor if a new application link is discovered
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
     */
    @EventListener
    public void onApplicationLinkRemoved(ApplicationLinkDeletedEvent event)
    {
        if (event.getApplicationType() instanceof RemotePluginContainerApplicationType)
        {
            accessors.remove(getPluginKey(event.getApplicationLink()));
        }
    }

    private String getPluginKey(ApplicationLink link)
    {
        return String.valueOf(link.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY));
    }

    /**
     * Supplies an accessor for remote plugin operations. Instances are only meant to be used for the current operation
     * and should not be cached across operations.
     *
     * @param pluginKey The plugin key
     * @return An accessor for either local or remote plugin operations
     */
    public RemotablePluginAccessor get(String pluginKey)
    {
        final Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        return get(plugin, pluginKey);
    }

    @Override
    public RemotablePluginAccessor get(Plugin plugin)
    {
        return get(plugin,plugin.getKey());
    }

    private RemotablePluginAccessor get(final Plugin plugin, final String pluginKey)
    {
        // this will potentially create multiple instances if called quickly, but we don't really
        // care as they shouldn't be cached
        final RemotablePluginAccessor accessor;
        if (accessors.containsKey(pluginKey))
        {
            accessor = accessors.get(pluginKey);
        }
        else
        {
            accessor = create(plugin, pluginKey, getDisplayUrl(pluginKey));
            accessors.put(pluginKey, accessor);
        }
        return accessor;
    }

    @Override
    public RemotablePluginAccessor getOrThrow(final String pluginKey)
    {
        RemotablePluginAccessor remotablePluginAccessor = get(pluginKey);
        if (remotablePluginAccessor == null)
        {
            throw new IllegalStateException("No " + RemotablePluginAccessor.class + " available for " + pluginKey);
        }
        return remotablePluginAccessor;
    }

    @Override
    public void remove(String pluginKey)
    {
        accessors.remove(pluginKey);
    }

    private Supplier<URI> getDisplayUrl(final String pluginKey)
    {
        final String storedBaseUrl = connectAddonRegistry.getBaseUrl(pluginKey);

        if (!Strings.isNullOrEmpty(storedBaseUrl))
        {
            return Suppliers.compose(ToUriFunction.INSTANCE,
                    new Supplier<String>()
                    {
                        @Override
                        public String get()
                        {
                            return storedBaseUrl;
                        }
                    }
            );
        }
        else
        {
            return Suppliers.compose(ToUriFunction.INSTANCE,
                    new Supplier<String>()
                    {
                        @Override
                        public String get()
                        {
                            return applicationProperties.getBaseUrl(UrlMode.CANONICAL);
                        }
                    }
            );
        }
    }

    /**
     * Supplies an accessor for remote plugin operations but always creates a new one.  Instances are still only meant
     * to be used for the current operation and should not be cached across operations.
     * <p/>
     * This method is useful for when the display url is known but the application link has not yet been created
     *
     * @param pluginKey The plugin key
     * @param displayUrl The display url
     * @return An accessor for a remote plugin
     */
    public RemotablePluginAccessor create(Plugin plugin, String pluginKey, Supplier<URI> displayUrl)
    {
        
        checkNotNull(plugin, "Plugin not found: '%s'", pluginKey);

        ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);
        AuthenticationMethod authenticationMethod = null;
        if (null == appLink)
        {
            log.error("Found no app link by plugin key '{}'!", pluginKey);
        }
        else
        {
            Object authTypeProperty = appLink.getProperty(AuthenticationMethod.PROPERTY_NAME);
            if (authTypeProperty != null)
            {
                authenticationMethod = AuthenticationMethod.forName(authTypeProperty.toString());
            }
        }

        if (AuthenticationMethod.JWT.equals(authenticationMethod))
        {
            return new JwtSigningRemotablePluginAccessor(plugin, displayUrl, jwtService, consumerService,
                    connectApplinkManager, httpContentRetriever, userManager);
        }
        else if (AuthenticationMethod.NONE.equals(authenticationMethod))
        {
            return new NoAuthRemotablePluginAccessor(plugin, displayUrl, httpContentRetriever);
        }
        else
        {
            // default to OAuth (for backwards compatibility)
            return new OAuthSigningRemotablePluginAccessor(plugin, displayUrl, getDummyServiceProvider(),
                    httpContentRetriever, oAuthLinkManager);
        }
    }

    private ServiceProvider getDummyServiceProvider()
    {
        URI dummyUri = URI.create("http://localhost");
        return new ServiceProvider(dummyUri, dummyUri, dummyUri);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    private static enum ToUriFunction implements Function<String, URI>
    {
        INSTANCE;

        @Override
        public URI apply(String uri)
        {
            return URI.create(uri);
        }
    }
}
