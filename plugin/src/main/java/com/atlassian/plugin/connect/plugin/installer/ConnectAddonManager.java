package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.*;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.UriBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

/**
 * The ConnectAddonManager handles all the stuff that needs to happen when an addon is enabled/disabled.
 *
 * @see com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler for legacy xml-descriptor add-ons.
 */
@Named
public class ConnectAddonManager
{
    private static final Logger log = LoggerFactory.getLogger(ConnectAddonManager.class);
    private static final String HTTP_ERROR_I18N_KEY_PREFIX = "connect.install.error.remote.host.bad.response.";
    private static final List<Integer> OK_INSTALL_HTTP_CODES = asList(200, 201, 204);

    private int testConnectionTimeout = 5 * 1000;
    private int testSocketTimeout = 5 * 1000;
    private int testRequestTimeout = 5 * 3000;

    public static final String USE_TEST_HTTP_CLIENT = "use.test.http.client";

    public static final String USER_KEY = "user_key";

    public enum SyncHandler
    {
        INSTALLED, UNINSTALLED, ENABLED, DISABLED
    }

    private final IsDevModeService isDevModeService;
    private final UserManager userManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private HttpClient httpClient;
    private final ConnectAddonRegistry addonRegistry;
    private final BeanToModuleRegistrar beanToModuleRegistrar;
    private final ConnectAddOnUserService connectAddOnUserService;
    private final EventPublisher eventPublisher;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;
    private final LicenseRetriever licenseRetriever;
    private final ProductAccessor productAccessor;
    private final BundleContext bundleContext;
    private final ConnectApplinkManager connectApplinkManager;
    private final I18nResolver i18nResolver;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final SharedSecretService sharedSecretService;
    private final ConnectAddonI18nManager i18nManager;

    private final AtomicBoolean isTestHttpClient;

    @Inject
    public ConnectAddonManager(IsDevModeService isDevModeService, UserManager userManager,
                               RemotablePluginAccessorFactory remotablePluginAccessorFactory, HttpClient httpClient, ConnectAddonRegistry addonRegistry,
                               BeanToModuleRegistrar beanToModuleRegistrar, ConnectAddOnUserService connectAddOnUserService,
                               EventPublisher eventPublisher, ConsumerService consumerService, ApplicationProperties applicationProperties,
                               LicenseRetriever licenseRetriever, ProductAccessor productAccessor, BundleContext bundleContext,
                               ConnectApplinkManager connectApplinkManager, I18nResolver i18nResolver, ConnectAddonBeanFactory connectAddonBeanFactory, SharedSecretService sharedSecretService, HttpClientFactory httpClientFactory, ConnectAddonI18nManager i18nManager)
    {
        this.isDevModeService = isDevModeService;
        this.userManager = userManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.httpClient = httpClient;
        this.addonRegistry = addonRegistry;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.connectAddOnUserService = connectAddOnUserService;
        this.eventPublisher = eventPublisher;
        this.consumerService = consumerService;
        this.applicationProperties = applicationProperties;
        this.licenseRetriever = licenseRetriever;
        this.productAccessor = productAccessor;
        this.bundleContext = bundleContext;
        this.connectApplinkManager = connectApplinkManager;
        this.i18nResolver = i18nResolver;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
        this.sharedSecretService = sharedSecretService;
        this.i18nManager = i18nManager;

        this.isTestHttpClient = new AtomicBoolean(false);

        if (Boolean.parseBoolean(System.getProperty(USE_TEST_HTTP_CLIENT, "false")) && !isTestHttpClient.get())
        {
            HttpClientOptions options = new HttpClientOptions();
            options.setConnectionTimeout(testConnectionTimeout, TimeUnit.MILLISECONDS);
            options.setRequestTimeout(testRequestTimeout, TimeUnit.MILLISECONDS);
            options.setSocketTimeout(testSocketTimeout, TimeUnit.MILLISECONDS);

            this.httpClient = httpClientFactory.create(options);
            this.isTestHttpClient.set(true);
        }
    }

    public boolean hasDescriptor(String pluginKey)
    {
        return addonRegistry.hasDescriptor(pluginKey);
    }

    public Iterable<String> getAllAddonKeys()
    {
        return addonRegistry.getAllAddonKeys();
    }

    public Iterable<ConnectAddonBean> getAllAddonBeans()
    {
        return addonRegistry.getAllAddonBeans();
    }

    /**
     * This method is public for test visibility. In preference, please use {@link ConnectAddOnInstaller#install(String)}
     * @param jsonDescriptor the json descriptor of the add-on to install
     * @return a {@link ConnectAddonBean} representation of the add-on
     */
    public ConnectAddonBean installConnectAddon(String jsonDescriptor, PluginState targetState)
    {
        long startTime = System.currentTimeMillis();
        

        Map<String, String> i18nCollector = newHashMap();
        ConnectAddonBean addOn = connectAddonBeanFactory.fromJson(jsonDescriptor,i18nCollector);

        String pluginKey = addOn.getKey();
        
        AddonSettings previousSettings = addonRegistry.getAddonSettings(pluginKey);
        ApplicationLink previousApplink = connectApplinkManager.getAppLink(pluginKey);

        if(!i18nCollector.isEmpty())
        {
            try
            {
                i18nManager.add(pluginKey,i18nCollector);
            }
            catch (IOException e)
            {
                //just logging for now... do we really want to throw for this?
                log.error("Unable to write i18n props for addon '" + pluginKey + "'",e);
            }
        }

        String previousDescriptor = addonRegistry.getDescriptor(pluginKey);

        AuthenticationType authType = addOn.getAuthentication().getType();
        final boolean useSharedSecret = addOnUsesSymmetricSharedSecret(authType); // TODO ACDEV-378: also check the algorithm
        String sharedSecret = useSharedSecret ? sharedSecretService.next() : null;
        String addOnSigningKey = useSharedSecret ? sharedSecret : addOn.getAuthentication().getPublicKey(); // the key stored on the applink: used to sign outgoing requests and verify incoming requests

        String userKey = addOnNeedsAUser(addOn) ? provisionAddOnUserAndScopes(addOn, previousDescriptor) : null;

        AddonSettings settings = new AddonSettings()
                .setAuth(authType.name())
                .setBaseUrl(addOn.getBaseUrl())
                .setDescriptor(jsonDescriptor)
                .setRestartState(PluginState.DISABLED.name())
                .setUserKey(userKey);

        if (!Strings.isNullOrEmpty(sharedSecret))
        {
            settings.setSecret(sharedSecret);
        }

        addonRegistry.storeAddonSettings(pluginKey, settings);

        //applink MUST be created before any modules but AFTER we store the settings
        connectApplinkManager.createAppLink(addOn, addOn.getBaseUrl(), authType, addOnSigningKey, userKey);

        //make the sync callback if needed
        if (!Strings.isNullOrEmpty(addOn.getLifecycle().getInstalled()))
        {
            requestInstallCallback(addOn, sharedSecret);
        }

        eventPublisher.publish(new ConnectAddonInstalledEvent(pluginKey));

        long endTime = System.currentTimeMillis();
        log.info("Connect addon '" + addOn.getKey() + "' installed in " + (endTime - startTime) + "ms");

        if (PluginState.ENABLED == targetState)
        {
            enableConnectAddon(pluginKey);
        }

        return addOn;
    }

    public void enableConnectAddon(final String pluginKey) throws ConnectAddOnUserInitException
    {
        long startTime = System.currentTimeMillis();
        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
        remotablePluginAccessorFactory.remove(pluginKey);

        //if a descriptor is not stored, it means this event was fired during install before modules were created and we need to ignore
        if (addonRegistry.hasDescriptor(pluginKey))
        {
            ConnectAddonBean addon = unmarshallDescriptor(pluginKey);

            if (null != addon)
            {
                beanToModuleRegistrar.registerDescriptorsForBeans(addon);

                if (addOnNeedsAUser(addon))
                {
                    enableAddOnUser(addon);
                }

                addonRegistry.storeRestartState(pluginKey, PluginState.ENABLED);
                eventPublisher.publish(new ConnectAddonEnabledEvent(pluginKey, createEventData(pluginKey, SyncHandler.ENABLED.name().toLowerCase())));

                long endTime = System.currentTimeMillis();
                log.info("Connect addon '" + addon.getKey() + "' enabled in " + (endTime - startTime) + "ms");
            }
            else
            {
                String message = "Tried to publish plugin enabled event for connect addon ['" + pluginKey + "'], but got a null ConnectAddonBean when trying to deserialize it's stored descriptor. Ignoring...";
                eventPublisher.publish(new ConnectAddonEnableFailedEvent(pluginKey, message));
                log.warn(message);
            }
        }
    }

    public void disableConnectAddon(final String pluginKey) throws ConnectAddOnUserDisableException
    {
        disableConnectAddon(pluginKey, true, true);
    }

    public void disableConnectAddonWithoutPersistingState(final String pluginKey)
            throws ConnectAddOnUserDisableException
    {
        disableConnectAddon(pluginKey, false, true);
    }

    private void disableConnectAddon(final String pluginKey, boolean persistState, boolean sendEvent)
            throws ConnectAddOnUserDisableException
    {
        long startTime = System.currentTimeMillis();
        remotablePluginAccessorFactory.remove(pluginKey);

        if (addonRegistry.hasDescriptor(pluginKey))
        {
            if (sendEvent)
            {
                //need to publish the event before we actually disable anything
                eventPublisher.publish(new ConnectAddonDisabledEvent(pluginKey, createEventData(pluginKey, SyncHandler.DISABLED.name().toLowerCase())));
            }

            disableAddOnUser(pluginKey);
            beanToModuleRegistrar.unregisterDescriptorsForAddon(pluginKey);

            if (persistState)
            {
                addonRegistry.storeRestartState(pluginKey, PluginState.DISABLED);
            }

            long endTime = System.currentTimeMillis();
            log.info("Connect addon '" + pluginKey + "' disabled in " + (endTime - startTime) + "ms");
        }
    }

    public boolean isAddonEnabled(String pluginKey)
    {
        return beanToModuleRegistrar.descriptorsAreRegistered(pluginKey);
    }

    public void uninstallConnectAddon(final String pluginKey) throws ConnectAddOnUserDisableException
    {
        uninstallConnectAddon(pluginKey, true);
    }

    public void uninstallConnectAddonQuietly(final String pluginKey)
    {
        try
        {
            uninstallConnectAddon(pluginKey, false);
        }
        catch (ConnectAddOnUserDisableException e)
        {
            //uh, don't you know what "quietly" means?
        }

    }

    private void uninstallConnectAddon(final String pluginKey, boolean sendEvent)
            throws ConnectAddOnUserDisableException
    {
        long startTime = System.currentTimeMillis();
        if (addonRegistry.hasDescriptor(pluginKey))
        {
            try
            {
                ConnectAddonBean addon = unmarshallDescriptor(pluginKey);

                disableConnectAddon(pluginKey, false, sendEvent);

                if (null != addon)
                {
                    if (sendEvent && !Strings.isNullOrEmpty(addon.getLifecycle().getUninstalled()))
                    {
                        try
                        {
                            callSyncHandler(addon, addon.getLifecycle().getUninstalled(), createEventDataForUninstallation(pluginKey, addon));
                        }
                        catch (LifecycleCallbackException e)
                        {
                            log.warn("Failed to notify remote host that add-on was uninstalled.", e);
                        }
                    }

                    if (sendEvent)
                    {
                        eventPublisher.publish(new ConnectAddonUninstalledEvent(pluginKey));
                    }

                    connectApplinkManager.deleteAppLink(addon);
                }
                else
                {
                    String message = "Tried to publish plugin uninstalled event for connect addon ['" + pluginKey + "'], but got a null ConnectAddonBean when trying to deserialize it's stored descriptor. Ignoring...";
                    if (sendEvent)
                    {
                        eventPublisher.publish(new ConnectAddonUninstallFailedEvent(pluginKey, message));
                    }
                    log.warn(message);
                }
            }
            finally
            {
                addonRegistry.removeAll(pluginKey);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Connect addon '" + pluginKey + "' uninstalled in " + (endTime - startTime) + "ms");
    }

    public ConnectAddonBean getExistingAddon(String pluginKey)
    {
        if (!addonRegistry.hasDescriptor(pluginKey))
        {
            return null;
        }

        String descriptor = addonRegistry.getDescriptor(pluginKey);
        return connectAddonBeanFactory.fromJsonSkipValidation(descriptor);
    }

    private void requestInstallCallback(ConnectAddonBean addon, String sharedSecret)
    {
        try
        {
            callSyncHandler(addon, addon.getLifecycle().getInstalled(), createEventDataForInstallation(addon.getKey(), sharedSecret, addon));
        }
        catch (LifecycleCallbackException e)
        {
            throw new PluginInstallException(e.getMessage(), e.getI18nKey());
        }
    }

    // removing the property from the app link removes the Authenticator's ability to assign a user to incoming requests
    // and as these users cannot log in anyway this reduces their possible actions to zero
    // (but don't remove the user as we need to preserve the history of their actions (e.g. audit trail, issue edited by <user>)
    private void disableAddOnUser(String addOnKey) throws ConnectAddOnUserDisableException
    {
        ApplicationLink applicationLink = connectApplinkManager.getAppLink(addOnKey);

        if (null != applicationLink)
        {
            applicationLink.removeProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
        }

        connectAddOnUserService.disableAddonUser(addOnKey);
    }

    private void enableAddOnUser(ConnectAddonBean addon) throws ConnectAddOnUserInitException
    {
        String userKey = connectAddOnUserService.getOrCreateUserKey(addon.getKey(), addon.getName());

        ApplicationLink applicationLink = connectApplinkManager.getAppLink(addon.getKey());

        if (null != applicationLink)
        {
            applicationLink.putProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME, userKey);
        }
        else
        {
            log.error("Unable to set the ApplicationLink user key property for add-on '{}' because the add-on has no ApplicationLink!", addon.getKey());
        }
    }

    // NB: the sharedSecret should be distributed synchronously and only on installation
    private void callSyncHandler(ConnectAddonBean addon, String path, String jsonEventData) throws LifecycleCallbackException
    {
        String callbackUrl = addon.getBaseUrl() + path;

        // try distributing prod shared secrets over http (note the lack of "s") and it shall be rejected
        if (!isDevModeService.isDevMode() && null != addon.getAuthentication() && AuthenticationType.JWT.equals(addon.getAuthentication().getType()) && !callbackUrl.toLowerCase().startsWith("https"))
        {
            String message = String.format("Cannot issue callback except via HTTPS. Current base URL = '%s'", addon.getBaseUrl());
            throw new LifecycleCallbackException(message, Option.some("connect.remote.upm.install.exception"));
        }

        Response response = getSyncHandlerResponse(addon, callbackUrl, jsonEventData);

        final int statusCode = response.getStatusCode();
        // a selection of 2xx response codes both indicate success and are semantically valid for this callback
        if (!OK_INSTALL_HTTP_CODES.contains(statusCode))
        {
            String statusText = response.getStatusText();
            String responseEntity = response.getEntity(); // calling response.getEntity() multiple times results in IllegalStateException("Entity may only be accessed once")
            log.error("Error contacting remote application at " + callbackUrl + " " + statusCode + ":[" + statusText + "]:" + responseEntity);

            String message = "Error contacting remote application " + statusCode + ":[" + statusText + "]:" + responseEntity;
            throw new LifecycleCallbackException(message, findI18nKeyForHttpErrorCode(statusCode));
        }
    }

    private Response getSyncHandlerResponse(ConnectAddonBean addon, String callbackUrl, String jsonEventData) throws LifecycleCallbackException
    {
        try
        {
            URI installHandler = getURI(callbackUrl);

            Request.Builder request = httpClient.newRequest(installHandler);
            request.setAttribute("purpose", "web-hook-notification");
            request.setAttribute("pluginKey", addon.getKey());
            request.setContentType(MediaType.APPLICATION_JSON);
            request.setEntity(jsonEventData);

            // It's important to use the plugin in the call to remotablePluginAccessorFactory.get(plugin) as we might be calling this due to an uninstall event
            com.atlassian.fugue.Option<String> authHeader = remotablePluginAccessorFactory.get(addon).getAuthorizationGenerator().generate(HttpMethod.POST, installHandler, Collections.<String, String[]>emptyMap());
            if (authHeader.isDefined())
            {
                request.setHeader(AUTHORIZATION_HEADER, authHeader.get());
            }

            return request.execute(Request.Method.POST).claim();
        }
        catch (Exception e)
        {
            log.error("Error contacting remote application at " + callbackUrl + "  [" + e.getMessage() + "]", e);
            String message = "Error contacting remote application [" + e.getMessage() + "]";

            if (e.getCause() instanceof UnknownHostException)
            {
                String i18nMessage = i18nResolver.getText("connect.install.error.remote.host.bad.domain", e.getCause().getLocalizedMessage().replace(": Name or service not known", ""));
                throw new LifecycleCallbackException(message, Option.some(i18nMessage));
            }
            else if (e.getCause() instanceof SocketTimeoutException)
            {
                String i18nMessage = i18nResolver.getText("connect.install.error.remote.host.timeout", callbackUrl);
                throw new LifecycleCallbackException(message, Option.some(i18nMessage));
            }

            throw new LifecycleCallbackException(message, Option.some("connect.remote.upm.install.exception"));
        }
    }

    private Option<String> findI18nKeyForHttpErrorCode(final int responseCode)
    {
        String i18nKey = HTTP_ERROR_I18N_KEY_PREFIX + responseCode;
        final String i18nText = i18nResolver.getRawText(i18nKey);

        // the I18nResolver javadoc says that it will return the input key as the output raw text when the key is not found
        // and we also check a couple of other obvious problems, just to be save
        if (StringUtils.isEmpty(i18nText) || i18nKey.equals(i18nText))
        {
            // fall back to the generic i18n key if there's no i18n key for this HTTP code
            i18nKey = "connect.remote.upm.install.exception";
        }

        return Option.some(i18nKey);
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

    /**
     * @param pluginKey the key of a Connect addon
     * @return a {@link ConnectAddonBean} if there is a corresponding descriptor stored in the registry, otherwise null
     */
    private ConnectAddonBean unmarshallDescriptor(final String pluginKey)
    {
        return ConnectModulesGsonFactory.getGson().fromJson(addonRegistry.getDescriptor(pluginKey), ConnectAddonBean.class);
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
                //noinspection deprecation
                dataBuilder.withUserKey(user.getUserKey().getStringValue());
            }

            dataBuilder.withLink("oauth", nullToEmpty(baseUrl) + "/rest/atlassian-connect/latest/oauth");
        }

        ConnectAddonEventData data = dataBuilder.build();

        return ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create().toJson(data);
    }

    private String getConnectPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
    }

    private boolean addOnUsesSymmetricSharedSecret(AuthenticationType authType)
    {
        return AuthenticationType.JWT.equals(authType);
    }

    private String provisionAddOnUserAndScopes(ConnectAddonBean addOn, String previousDescriptor)
            throws PluginInstallException
    {
        Set<ScopeName> previousScopes = Sets.newHashSet();
        Set<ScopeName> newScopes = addOn.getScopes();

        if (StringUtils.isNotBlank(previousDescriptor))
        {
            ConnectAddonBean previousAddOn = connectAddonBeanFactory.fromJson(previousDescriptor);
            previousScopes = previousAddOn.getScopes();
        }

        try
        {
            return connectAddOnUserService.provisionAddonUserForScopes(addOn.getKey(), addOn.getName(), previousScopes, newScopes);
        }
        catch (ConnectAddOnUserInitException e)
        {
            throw new PluginInstallException(e.getMessage(), Option.some("connect.install.error.user.provisioning"), e, true);
        }
    }

    private static boolean addOnNeedsAUser(ConnectAddonBean addOn)
    {
        return null != addOn.getAuthentication() && !AuthenticationType.NONE.equals(addOn.getAuthentication().getType());
    }
}
