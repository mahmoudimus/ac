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
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.google.common.base.Function;
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
    private final OAuthLinkManager oAuthLinkManager;
    private final CachingHttpContentRetriever httpContentRetriever;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private final JwtService jwtService;
    private final ConsumerService consumerService;

    private final Map<String, RemotablePluginAccessor> accessors;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemotablePluginAccessorFactory.class);

    @Autowired
    public DefaultRemotablePluginAccessorFactory(ConnectApplinkManager connectApplinkManager,
                                                 OAuthLinkManager oAuthLinkManager,
                                                 CachingHttpContentRetriever httpContentRetriever,
                                                 PluginAccessor pluginAccessor,
                                                 ApplicationProperties applicationProperties,
                                                 EventPublisher eventPublisher,
                                                 JwtService jwtService,
                                                 ConsumerService consumerService)
    {
        this.connectApplinkManager = connectApplinkManager;
        this.oAuthLinkManager = oAuthLinkManager;
        this.httpContentRetriever = httpContentRetriever;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.consumerService = consumerService;
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

    /**
     * Clear accessor if a plugin is enabled
     */
    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event)
    {
        accessors.remove(event.getPlugin().getKey());
    }

    /**
     * Clear accessor if a plugin is disabled
     */
    @EventListener
    public void onPluginDisabled(PluginDisabledEvent event)
    {
        accessors.remove(event.getPlugin().getKey());
    }

    @EventListener
    public void onPluginUninstalled(PluginUninstalledEvent event)
    {
        // this method is invoked for every plugin uninstall. The ConnectApplinkManager ensures we only remove applinks
        // for connect add-ons
        Plugin plugin = event.getPlugin();
        String key = plugin.getKey();

        try
        {
            connectApplinkManager.deleteAppLink(plugin);
            accessors.remove(key);
        }
        catch (NotConnectAddonException e)
        {
            // swallow error, we don't want to do anything for plugins that are not connect add-ons.
        }
    }

    @PluginEventListener
    public void onPluginModuleEnabled(PluginModuleEnabledEvent event)
    {
        accessors.remove(event.getModule().getPluginKey());
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
        // this will potentially create multiple instances if called quickly, but we don't really
        // care as they shouldn't be cached
        final RemotablePluginAccessor accessor;
        if (accessors.containsKey(pluginKey))
        {
            accessor = accessors.get(pluginKey);
        }
        else
        {
            accessor = create(pluginKey, getDisplayUrl(pluginKey));
            accessors.put(pluginKey, accessor);
        }
        return accessor;
    }

    private Supplier<URI> getDisplayUrl(final String pluginKey)
    {
        final ApplicationLink link = connectApplinkManager.getAppLink(pluginKey);
        if (link != null)
        {
            return Suppliers.ofInstance(link.getDisplayUrl());
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
    public RemotablePluginAccessor create(String pluginKey, Supplier<URI> displayUrl)
    {
        final Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        checkNotNull(plugin, "Plugin not found: '%s'", pluginKey);

        return signsWithJwt(pluginKey)
                // don't need to get the actual provider as it doesn't really matter
                ? new JwtSigningRemotablePluginAccessor(pluginKey, plugin.getName(), displayUrl, jwtService, consumerService, connectApplinkManager, httpContentRetriever)
                : new OAuthSigningRemotablePluginAccessor(pluginKey, plugin.getName(), displayUrl, getDummyServiceProvider(), httpContentRetriever, oAuthLinkManager);
    }

    private boolean signsWithJwt(String pluginKey)
    {
        ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);
        Object authTypeProperty = null;

        if (null == appLink)
        {
            log.error("Found no app link by plugin key '{}'!", pluginKey);
        }
        else
        {
            authTypeProperty = appLink.getProperty(AuthenticationMethod.PROPERTY_NAME);
        }

        // for backwards compatibility default to "not JWT" if the property does not exist
        return null != authTypeProperty && AuthenticationMethod.JWT.equals(AuthenticationMethod.forName(authTypeProperty.toString()));
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
