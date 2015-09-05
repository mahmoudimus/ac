package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.JwtService;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import com.atlassian.plugin.connect.spi.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class DefaultRemotablePluginAccessorFactoryImpl implements DefaultRemotablePluginAccessorFactory
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
    private final ConnectAddonBeanFactory connectAddonBeanFactory;

    private final Map<String, RemotablePluginAccessor> accessors;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemotablePluginAccessorFactoryImpl.class);

    @Autowired
    public DefaultRemotablePluginAccessorFactoryImpl(ConnectApplinkManager connectApplinkManager,
                                                 ConnectAddonRegistry connectAddonRegistry,
                                                 OAuthLinkManager oAuthLinkManager,
                                                 CachingHttpContentRetriever httpContentRetriever,
                                                 PluginAccessor pluginAccessor,
                                                 ApplicationProperties applicationProperties,
                                                 EventPublisher eventPublisher,
                                                 JwtService jwtService,
                                                 ConsumerService consumerService,
                                                 UserManager userManager, 
                                                 ConnectAddonBeanFactory connectAddonBeanFactory)
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
        this.connectAddonBeanFactory = connectAddonBeanFactory;
        this.eventPublisher.register(this);
        this.jwtService = jwtService;

        this.accessors = CopyOnWriteMap.newHashMap();
    }

    /**
     * Clear accessor if a new application link is discovered
     *
     * @param event the event fired
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
     *
     * @param event the event fired
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

    public RemotablePluginAccessor get(String pluginKey)
    {
        if(connectAddonRegistry.hasDescriptor(pluginKey))
        {
            return get(connectAddonBeanFactory.fromJsonSkipValidation(connectAddonRegistry.getDescriptor(pluginKey)));
        }
        
        final Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        return get(plugin, pluginKey);
    }

    public RemotablePluginAccessor get(ConnectAddonBean addon)
    {
        // this will potentially create multiple instances if called quickly, but we don't really
        // care as they shouldn't be cached
        final RemotablePluginAccessor accessor;
        if (accessors.containsKey(addon.getKey()))
        {
            accessor = accessors.get(addon.getKey());
        }
        else
        {
            accessor = create(addon, getDisplayUrl(addon));
            accessors.put(addon.getKey(), accessor);
        }
        return accessor;
    }

    /**
     * @deprecated use {@code get(String pluginKey)} or {@code get(ConnectAddonBean addon)} instead
     */
    @Deprecated
    @Override
    public RemotablePluginAccessor get(Plugin plugin)
    {
        return get(plugin, plugin.getKey());
    }

    /**
     * @deprecated use {@code get(String pluginKey)} or {@code get(ConnectAddonBean addon)} instead
     */
    @Deprecated
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

    private Supplier<URI> getDisplayUrl(final ConnectAddonBean addon)
    {
        final String storedBaseUrl = addon.getBaseUrl();

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
     * @deprecated use {@code getDisplayUrl(ConnectAddonBean addon)} instead
     */
    @Deprecated
    private Supplier<URI> getDisplayUrl(final String pluginKey)
    {
        String addonBaseUrl = "";

        if(connectAddonRegistry.hasBaseUrl(pluginKey))
        {
            addonBaseUrl = connectAddonRegistry.getBaseUrl(pluginKey);
        }
        else
        {
            throw new IllegalStateException(pluginKey + " appears to be an XML add-on");
        }

        final String storedBaseUrl = addonBaseUrl;

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
     *
     * This method is useful for when the display url is known but the application link has not yet been created
     *
     * @param addon The addon bean
     * @param displayUrl The display url
     * @return An accessor for a remote plugin
     *
     * @deprecated use {@code create(ConnectAddonBean addon, Supplier<URI> displayUrl)} instead
     */
    @Deprecated
    @Override
    public RemotablePluginAccessor create(ConnectAddonBean addon, Supplier<URI> displayUrl)
    {
        ApplicationLink appLink = connectApplinkManager.getAppLink(addon.getKey());
        AuthenticationMethod authenticationMethod = null;
        if (null == appLink)
        {
            log.error("Found no app link by plugin key '{}'!", addon.getKey());
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
            return new JwtSigningRemotablePluginAccessor(addon, displayUrl, jwtService, consumerService,
                    connectApplinkManager, httpContentRetriever, userManager);
        }
        else if (AuthenticationMethod.NONE.equals(authenticationMethod))
        {
            return new NoAuthRemotablePluginAccessor(addon, displayUrl, httpContentRetriever);
        }
        else
        {
            // default to OAuth (for backwards compatibility)
            return new OAuthSigningRemotablePluginAccessor(addon, displayUrl, getDummyServiceProvider(),
                    httpContentRetriever, oAuthLinkManager);
        }
    }

    /**
     * Supplies an accessor for remote plugin operations but always creates a new one.  Instances are still only meant
     * to be used for the current operation and should not be cached across operations.
     *
     * This method is useful for when the display url is known but the application link has not yet been created
     *
     * @param plugin the plugin to access
     * @param pluginKey The plugin key
     * @param displayUrl The display url
     * @return An accessor for a remote plugin
     *
     * @deprecated use {@code create(ConnectAddonBean addon, Supplier<URI> displayUrl)} instead
     */
    @Deprecated
    @Override
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
            final ConnectAddonBean addon = connectAddonBeanFactory.fromJsonSkipValidation(connectAddonRegistry.getDescriptor(pluginKey));
            return new JwtSigningRemotablePluginAccessor(addon, displayUrl, jwtService, consumerService,
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
