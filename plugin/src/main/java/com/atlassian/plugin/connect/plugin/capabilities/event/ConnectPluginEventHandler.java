package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonEnabledEvent;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.UriBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * This is the central place to handle PluginEvents broadcasted from plug-core as they relate to connect addons. Any
 * PluginEvent should be handled here. This is essentially the implementation of the addon lifecycle.
 */
@Named
public class ConnectPluginEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginEventHandler.class);
    public static final String USER_KEY = "user_key";

    private final EventPublisher eventPublisher;
    private final PluginEventManager pluginEventManager;
    private final UserManager userManager;
    private final HttpClient httpClient;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;
    private final ProductAccessor productAccessor;
    private final BundleContext bundleContext;
    private final JsonConnectAddOnIdentifierService connectIdentifier;
    private final ConnectAddonRegistry descriptorRegistry;
    private final BeanToModuleRegistrar beanToModuleRegistrar;
    private final LicenseRetriever licenseRetriever;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddOnUserService connectAddOnUserService;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;

    public enum SyncHandler
    {
        INSTALLED, UNINSTALLED, ENABLED, DISABLED
    }

    @Inject
    public ConnectPluginEventHandler(EventPublisher eventPublisher,
            PluginEventManager pluginEventManager,
            UserManager userManager,
            HttpClient httpClient,
            ConsumerService consumerService,
            ApplicationProperties applicationProperties,
            ProductAccessor productAccessor,
            BundleContext bundleContext,
            JsonConnectAddOnIdentifierService connectIdentifier,
            ConnectAddonRegistry descriptorRegistry,
            BeanToModuleRegistrar beanToModuleRegistrar,
            LicenseRetriever licenseRetriever,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            JwtApplinkFinder jwtApplinkFinder, ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService,
            ConnectAddonBeanFactory connectAddonBeanFactory)
    {
        this.eventPublisher = eventPublisher;
        this.pluginEventManager = pluginEventManager;
        this.licenseRetriever = licenseRetriever;
        this.userManager = userManager;
        this.httpClient = httpClient;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.consumerService = consumerService;
        this.applicationProperties = applicationProperties;
        this.productAccessor = productAccessor;
        this.bundleContext = bundleContext;
        this.connectIdentifier = connectIdentifier;
        this.descriptorRegistry = descriptorRegistry;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.jwtApplinkFinder = jwtApplinkFinder;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddOnUserService = connectAddOnUserService;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
    }

    /**
     * Called directly from the {@link com.atlassian.plugin.connect.plugin.installer.ConnectUPMInstallHandler} when a
     * plugin is installed. This needs to be called manually instead of listening for a PluginInstalledEvent as we do
     * special handling
     *
     * @param plugin The plugin that was installed
     * @param addon The addon bean we're installing
     * @param sharedSecret The addon's shared secret if it is JWT
     */
    public void pluginInstalled(Plugin plugin, ConnectAddonBean addon, String sharedSecret)
    {
        if (!Strings.isNullOrEmpty(addon.getLifecycle().getInstalled()))
        {
            callSyncHandler(plugin, addon, addon.getLifecycle().getInstalled(), createEventDataForInstallation(addon.getKey(), sharedSecret, addon), SyncHandler.INSTALLED);
        }
    }

    /**
     * This is called by the plugin system during installs and enables. We may need to ignore this until connect
     * specific stuff is setup.
     */
    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent) throws ConnectAddOnUserInitException
    {
        final Plugin plugin = pluginEnabledEvent.getPlugin();
        String pluginKey = plugin.getKey();

        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
        remotablePluginAccessorFactory.remove(pluginKey);

        //if a descriptor is not stored, it means this event was fired during install before modules were created and we need to ignore
        if (connectIdentifier.isConnectAddOn(plugin) && descriptorRegistry.hasDescriptor(pluginKey))
        {
            ConnectAddonBean addon;
            try
            {
                // unmarshall and validate the stored descriptor
                addon = connectAddonBeanFactory.fromJson(descriptorRegistry.getDescriptor(pluginKey));
            }
            catch (InvalidDescriptorException e)
            {
                log.warn("Failed to validate stored JSON descriptor for " + plugin.getKey() +
                        ". Module descriptors will not be registered.", e);
                throw e;
            }

            if (null != addon)
            {
                beanToModuleRegistrar.registerDescriptorsForBeans(plugin, addon);
                connectAddOnUserService.getOrCreateUserKey(pluginKey);
                publishEnabledEvent(pluginKey);
            }
            else
            {
                log.warn("Tried to publish plugin enabled event for connect addon ['" + pluginKey + "'], but got a null ConnectAddonBean when trying to deserialize it's stored descriptor. Ignoring...");
            }
        }
    }

    @PluginEventListener
    public void onPluginModuleEnabled(PluginModuleEnabledEvent event)
    {
        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
        remotablePluginAccessorFactory.remove(event.getModule().getPluginKey());
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginDisabled(BeforePluginDisabledEvent pluginDisabledEvent)
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            eventPublisher.publish(new ConnectAddonDisabledEvent(plugin.getKey(), createEventData(plugin.getKey(), SyncHandler.DISABLED.name().toLowerCase())));
        }
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginDisabled(PluginDisabledEvent pluginDisabledEvent) throws ConnectAddOnUserDisableException
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();

        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
        remotablePluginAccessorFactory.remove(plugin.getKey());

        if (connectIdentifier.isConnectAddOn(plugin))
        {
            beanToModuleRegistrar.unregisterDescriptorsForPlugin(plugin);
            disableAddOnUser(plugin.getKey());
        }

        // TODO remove this once we remove support for XML desciptors
        // ACDEV-886 -- unregister for ALL addons, as some XML descriptors register strategies
        iFrameRenderStrategyRegistry.unregisterAll(plugin.getKey());
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginUninstalled(PluginUninstalledEvent pluginUninstalledEvent) throws ConnectAddOnUserDisableException
    {
        final Plugin plugin = pluginUninstalledEvent.getPlugin();
        String pluginKey = plugin.getKey();

        if (descriptorRegistry.hasDescriptor(pluginKey))
        {
            // just unmarshall the descriptor without validating so we can actually uninstall invalid descriptors
            ConnectAddonBean addon = connectAddonBeanFactory.fromJsonSkipValidation(descriptorRegistry.getDescriptor(pluginKey));

            if (null != addon)
            {
                if (!Strings.isNullOrEmpty(addon.getLifecycle().getUninstalled()))
                {
                    try
                    {
                        callSyncHandler(plugin, addon, addon.getLifecycle().getUninstalled(), createEventDataForUninstallation(pluginKey, addon), SyncHandler.UNINSTALLED);
                    }
                    catch (PluginInstallException e)
                    {
                        log.warn("Failed to notify remote host that add-on was uninstalled.", e);
                    }
                }
            }
            else
            {
                log.warn("Tried to publish plugin uninstalled event for connect addon ['" + pluginKey + "'], but got a null ConnectAddonBean when trying to deserialize it's stored descriptor. Ignoring...");
            }
        }

        try
        {
            connectApplinkManager.deleteAppLink(plugin);
            remotablePluginAccessorFactory.remove(pluginKey);
        }
        catch (NotConnectAddonException e)
        {
            // swallow error, we don't want to do anything for plugins that are not connect add-ons.
        }

        descriptorRegistry.removeAll(pluginKey);
        disableAddOnUser(pluginKey);
    }

    // removing the property from the app link removes the Authenticator's ability to assign a user to incoming requests
    // and as these users cannot log in anyway this reduces their possible actions to zero
    // (but don't remove the user as we need to preserve the history of their actions (e.g. audit trail, issue edited by <user>)
    private void disableAddOnUser(String addOnKey) throws ConnectAddOnUserDisableException
    {
        ApplicationLink applicationLink = jwtApplinkFinder.find(addOnKey);

        if (null != applicationLink)
        {
            applicationLink.removeProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
        }
        else
        {
            log.error("Unable to disable the user for add-on '{}' because the add-on has no ApplicationLink!", addOnKey);
        }

        connectAddOnUserService.disableAddonUser(addOnKey);
    }

    public void publishEnabledEvent(String pluginKey)
    {
        eventPublisher.publish(new ConnectAddonEnabledEvent(pluginKey, createEventData(pluginKey, SyncHandler.ENABLED.name().toLowerCase())));
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        this.pluginEventManager.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        this.pluginEventManager.unregister(this);
    }

    // NB: the sharedSecret should be distributed synchronously and only on installation
    private void callSyncHandler(Plugin plugin, ConnectAddonBean addon, String path, String jsonEventData, SyncHandler handler)
    {
        String callbackUrl = addon.getBaseUrl() + path;

        try
        {
            String pluginKey = addon.getKey();

            URI installHandler = getURI(callbackUrl);

            Request.Builder request = httpClient.newRequest(installHandler);
            request.setAttribute("purpose", "web-hook-notification");
            request.setAttribute("pluginKey", pluginKey);
            request.setContentType(MediaType.APPLICATION_JSON);
            request.setEntity(jsonEventData);

            // It's important to use the plugin in the call to remotablePluginAccessorFactory.get(plugin) as we might be calling this due to an uninstall event
            com.atlassian.fugue.Option<String> authHeader = remotablePluginAccessorFactory.get(plugin).getAuthorizationGenerator().generate(HttpMethod.POST, installHandler, Collections.<String, String[]>emptyMap());
            if (authHeader.isDefined())
            {
                request.setHeader(AUTHORIZATION_HEADER, authHeader.get());
            }

            Response response = request.execute(Request.Method.POST).claim();
            int statusCode = response.getStatusCode();
            if (statusCode != 200 && statusCode != 204)
            {
                String statusText = response.getStatusText();
                log.error("Error contacting remote application at " + callbackUrl + " " + statusCode + ":[" + statusText + "]");

                String message = "Error contacting remote application " + statusCode + ":[" + statusText + "]";
                switch (handler)
                {
                    case INSTALLED:
                        throw new PluginInstallException(handler.name() + ": " + message,
                                Option.some("connect.install.error.remote.host.bad.response"));
                    case UNINSTALLED:
                        throw new PluginException(handler.name() + ": " + message);
                }
            }

        }
        // Catching Exception here makes me very sad, but there doesn't seem to be a contract for what RuntimeExceptions
        // are thrown by atlassian http-client. The type passed to the ResponseTransformation.Builder.fail() function is
        // a Throwable, so no help there. The upshot is we can't determine whether this exception was thrown because the
        // request to the remote host failed, or because there's a bug in our code. We'll assume the former for now..
        catch (Exception e)
        {
            log.error("Error contacting remote application at " + callbackUrl + "  [" + e.getMessage() + "]", e);

            String message = "Error contacting remote application [" + e.getMessage() + "]";
            switch (handler)
            {
                case INSTALLED:
                    throw new PluginInstallException(handler.name() + ": " + message,
                            Option.some("connect.install.error.remote.installation.error"));
                case UNINSTALLED:
                    throw new PluginException(handler.name() + ": " + message);
            }
        }
    }

    @VisibleForTesting
    String createEventData(String pluginKey, String eventType)
    {
        return createEventDataInternal(pluginKey, eventType, null, null);
    }

    String createEventDataForInstallation(String pluginKey, String sharedSecret, ConnectAddonBean addon)
    {
        return createEventDataInternal(pluginKey, SyncHandler.INSTALLED.name().toLowerCase(), sharedSecret, addon);
    }

    String createEventDataForUninstallation(String pluginKey, ConnectAddonBean addon)
    {
        return createEventDataInternal(pluginKey, SyncHandler.UNINSTALLED.name().toLowerCase(), null, addon);
    }

    // NB: the sharedSecret should be distributed synchronously and only on installation
    private String createEventDataInternal(String pluginKey, String eventType, String sharedSecret, ConnectAddonBean addon)
    {
        final Consumer consumer = checkNotNull(consumerService.getConsumer()); // checkNotNull() otherwise we NPE below

        ConnectAddonEventDataBuilder dataBuilder = newConnectAddonEventData();
        String baseUrl = applicationProperties.getBaseUrl(UrlMode.CANONICAL);

        dataBuilder.withBaseUrl(nullToEmpty(baseUrl))
                .withPluginKey(pluginKey)
                .withClientKey(nullToEmpty(consumer.getKey()))
                .withPublicKey(nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                .withSharedSecret(nullToEmpty(sharedSecret))
                .withPluginsVersion(nullToEmpty(getConnectPluginVersion()))
                .withServerVersion(nullToEmpty(applicationProperties.getBuildNumber()))
                .withServiceEntitlementNumber(nullToEmpty(licenseRetriever.getServiceEntitlementNumber(pluginKey)))
                .withProductType(nullToEmpty(productAccessor.getKey()))
                .withDescription(nullToEmpty(consumer.getDescription()))
                .withEventType(eventType);

        if (null != addon && null != addon.getAuthentication() && AuthenticationType.OAUTH.equals(addon.getAuthentication().getType()))
        {
            // Only add user_key
            UserProfile user = userManager.getRemoteUser();
            if (null != user)
            {
                dataBuilder.withUserKey(user.getUserKey().getStringValue());
            }

            dataBuilder.withLink("oauth", nullToEmpty(baseUrl) + "/rest/atlassian-connect/latest/oauth");
        }

        ConnectAddonEventData data = dataBuilder.build();

        return ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create().toJson(data);
    }

    private URI getURI(String url)
    {
        UriBuilder builder = new UriBuilder().setPath(url);

        UserProfile user = userManager.getRemoteUser();
        if (null != user)
        {
            builder.addQueryParameter(USER_KEY, user.getUserKey().getStringValue());
        }

        return builder.toUri().toJavaUri();
    }

    private String getConnectPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
    }
}
