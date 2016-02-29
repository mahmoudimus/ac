package com.atlassian.plugin.connect.plugin.request;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.request.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.auth.AuthenticationMethod;
import com.atlassian.plugin.connect.plugin.auth.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.auth.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.auth.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.connect.plugin.auth.jwt.JwtSigningRemotablePluginAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class DefaultRemotablePluginAccessorFactoryImpl implements DefaultRemotablePluginAccessorFactory {
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final CachingHttpContentRetriever httpContentRetriever;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private final JwtJsonBuilderFactory jwtBuilderFactory;
    private final JwtService jwtService;
    private final ConsumerService consumerService;

    private ConnectAddonAccessor addonAccessor;

    private final Map<String, RemotablePluginAccessor> accessors;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemotablePluginAccessorFactoryImpl.class);

    @Autowired
    public DefaultRemotablePluginAccessorFactoryImpl(ConnectApplinkManager connectApplinkManager,
                                                     ConnectAddonRegistry connectAddonRegistry,
                                                     CachingHttpContentRetriever httpContentRetriever,
                                                     PluginAccessor pluginAccessor,
                                                     ApplicationProperties applicationProperties,
                                                     EventPublisher eventPublisher,
                                                     JwtJsonBuilderFactory jwtBuilderFactory,
                                                     JwtService jwtService,
                                                     ConsumerService consumerService,
                                                     ConnectAddonAccessor addonAccessor) {
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddonRegistry = connectAddonRegistry;
        this.httpContentRetriever = httpContentRetriever;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.consumerService = consumerService;
        this.eventPublisher.register(this);
        this.jwtBuilderFactory = jwtBuilderFactory;
        this.jwtService = jwtService;
        this.addonAccessor = addonAccessor;

        this.accessors = CopyOnWriteMap.newHashMap();
    }

    /**
     * Clear accessor if a new application link is discovered
     *
     * @param event the event fired
     */
    @EventListener
    public void onApplicationLinkCreated(ApplicationLinkAddedEvent event) {
        if (event.getApplicationType() instanceof RemotePluginContainerApplicationType) {
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
    public void onApplicationLinkRemoved(ApplicationLinkDeletedEvent event) {
        if (event.getApplicationType() instanceof RemotePluginContainerApplicationType) {
            accessors.remove(getPluginKey(event.getApplicationLink()));
        }
    }

    private String getPluginKey(ApplicationLink link) {
        return String.valueOf(link.getProperty(DefaultConnectApplinkManager.PLUGIN_KEY_PROPERTY));
    }

    @Override
    public RemotablePluginAccessor get(final String pluginKey) {
        Optional<ConnectAddonBean> optionalAddon = addonAccessor.getAddon(pluginKey);
        if (optionalAddon.isPresent()) {
            return get(optionalAddon.get());
        }
        Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        return get(plugin, pluginKey);
    }

    private RemotablePluginAccessor get(ConnectAddonBean addon) {
        // this will potentially create multiple instances if called quickly, but we don't really
        // care as they shouldn't be cached
        final RemotablePluginAccessor accessor;
        if (accessors.containsKey(addon.getKey())) {
            accessor = accessors.get(addon.getKey());
        } else {
            accessor = create(addon, getDisplayUrl(addon));
            accessors.put(addon.getKey(), accessor);
        }
        return accessor;
    }

    private RemotablePluginAccessor get(final Plugin plugin, final String pluginKey) {
        // this will potentially create multiple instances if called quickly, but we don't really
        // care as they shouldn't be cached
        final RemotablePluginAccessor accessor;
        if (accessors.containsKey(pluginKey)) {
            accessor = accessors.get(pluginKey);
        } else {
            accessor = create(plugin, pluginKey, getDisplayUrl(pluginKey));
            accessors.put(pluginKey, accessor);
        }
        return accessor;
    }

    @Override
    public void remove(String pluginKey) {
        accessors.remove(pluginKey);
    }

    private Supplier<URI> getDisplayUrl(final ConnectAddonBean addon) {
        final String storedBaseUrl = addon.getBaseUrl();

        if (!Strings.isNullOrEmpty(storedBaseUrl)) {
            return Suppliers.compose(URI::create, () -> storedBaseUrl);
        } else {
            return Suppliers.compose(URI::create,
                    () -> applicationProperties.getBaseUrl(UrlMode.CANONICAL)
            );
        }
    }

    /**
     * @deprecated use {@code getDisplayUrl(ConnectAddonBean addon)} instead
     */
    @Deprecated
    private Supplier<URI> getDisplayUrl(final String pluginKey) {
        String addonBaseUrl;

        if (connectAddonRegistry.hasBaseUrl(pluginKey)) {
            addonBaseUrl = connectAddonRegistry.getBaseUrl(pluginKey);
        } else {
            throw new IllegalStateException(pluginKey + " appears to be an XML add-on");
        }

        final String storedBaseUrl = addonBaseUrl;

        if (!Strings.isNullOrEmpty(storedBaseUrl)) {
            return Suppliers.compose(URI::create, () -> storedBaseUrl);
        } else {
            return Suppliers.compose(URI::create,
                    () -> applicationProperties.getBaseUrl(UrlMode.CANONICAL)
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
     */
    private RemotablePluginAccessor create(ConnectAddonBean addon, Supplier<URI> displayUrl) {
        ApplicationLink appLink = connectApplinkManager.getAppLink(addon.getKey());
        AuthenticationMethod authenticationMethod = null;
        if (null == appLink) {
            log.error("Found no app link by plugin key '{}'!", addon.getKey());
        } else {
            Object authTypeProperty = appLink.getProperty(AuthenticationMethod.PROPERTY_NAME);
            if (authTypeProperty != null) {
                authenticationMethod = AuthenticationMethod.forName(authTypeProperty.toString());
            }
        }

        if (AuthenticationMethod.JWT.equals(authenticationMethod)) {
            return new JwtSigningRemotablePluginAccessor(addon, displayUrl, jwtBuilderFactory, jwtService,
                    consumerService, connectApplinkManager, httpContentRetriever);
        } else if (AuthenticationMethod.NONE.equals(authenticationMethod)) {
            return new NoAuthRemotablePluginAccessor(addon, displayUrl, httpContentRetriever);
        } else {
            throw new IllegalStateException("No valid authentication method found for " + addon.getKey() +
                    ".\nThis was probably caused by a data restore from a different instance.\n" +
                    "Please refer this instance to the Atlassian Connect developer on support for remediation.\n" +
                    "DO NOT simply re-install the affected add-ons; this can cause data loss for some add-ons.\n" +
                    "See https://ecosystem.atlassian.net/browse/AC-1528");
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
     */
    private RemotablePluginAccessor create(Plugin plugin, String pluginKey, Supplier<URI> displayUrl) {
        checkNotNull(plugin, "Plugin not found: '%s'", pluginKey);

        ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);
        AuthenticationMethod authenticationMethod = null;
        if (null == appLink) {
            log.error("Found no app link by plugin key '{}'!", pluginKey);
        } else {
            Object authTypeProperty = appLink.getProperty(AuthenticationMethod.PROPERTY_NAME);
            if (authTypeProperty != null) {
                authenticationMethod = AuthenticationMethod.forName(authTypeProperty.toString());
            }
        }

        if (AuthenticationMethod.JWT.equals(authenticationMethod)) {
            ConnectAddonBean addon = addonAccessor.getAddon(pluginKey).get();
            return new JwtSigningRemotablePluginAccessor(addon, displayUrl, jwtBuilderFactory, jwtService,
                    consumerService, connectApplinkManager, httpContentRetriever);
        } else if (AuthenticationMethod.NONE.equals(authenticationMethod)) {
            return new NoAuthRemotablePluginAccessor(plugin, displayUrl, httpContentRetriever);
        } else {
            throw new IllegalStateException("No valid authentication method found for " + plugin.getKey() +
                    ".\nThis was probably caused by a data restore from a different instance.\n" +
                    "Please contact Atlassian support for assistance getting your add-ons up and running again.");
        }
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }
}
