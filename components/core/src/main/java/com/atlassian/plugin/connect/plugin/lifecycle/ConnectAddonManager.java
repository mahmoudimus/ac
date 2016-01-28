package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.auth.ReKeyableAuthorizationGenerator;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonEnableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInstallException;
import com.atlassian.plugin.connect.api.request.HttpHeaderNames;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.AddonSettings;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.auth.SharedSecretService;
import com.atlassian.plugin.connect.plugin.auth.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.descriptor.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.lifecycle.event.*;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.request.ConnectHttpClientFactory;
import com.atlassian.plugin.connect.plugin.util.IsDevModeService;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
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
import javax.net.ssl.SSLException;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;

/**
 * The ConnectAddonManager handles all the stuff that needs to happen when an add-on is enabled/disabled.
 */
@Named
public class ConnectAddonManager
{
    private static final Logger log = LoggerFactory.getLogger(ConnectAddonManager.class);
    private static final String HTTP_ERROR_I18N_KEY_PREFIX = "connect.install.error.remote.host.bad.response.";
    private static final List<Integer> OK_INSTALL_HTTP_CODES = asList(200, 201, 204);
    private static final SigningAlgorithm JWT_ALGORITHM = SigningAlgorithm.HS256; // currently, this is the only algorithm that we support
    private static final String DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY = "connect.lifecycle.install.sign_with_prev_key.disable";

    private static final String USER_KEY = "user_key";

    public enum SyncHandler
    {
        INSTALLED, UNINSTALLED, ENABLED, DISABLED
    }

    private final IsDevModeService isDevModeService;
    private final UserManager userManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final ConnectAddonAccessor connectAddonAccessor;
    private HttpClient httpClient;
    private final ConnectAddonRegistry addonRegistry;
    private final BeanToModuleRegistrar beanToModuleRegistrar;
    private final ConnectUserService connectUserService;
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
    private final DarkFeatureManager darkFeatureManager;

    @Inject
    public ConnectAddonManager(IsDevModeService isDevModeService, UserManager userManager,
                               RemotablePluginAccessorFactory remotablePluginAccessorFactory, ConnectAddonRegistry addonRegistry,
                               BeanToModuleRegistrar beanToModuleRegistrar, ConnectUserService connectUserService,
                               EventPublisher eventPublisher, ConsumerService consumerService, ApplicationProperties applicationProperties,
                               LicenseRetriever licenseRetriever, ProductAccessor productAccessor, BundleContext bundleContext,
                               ConnectApplinkManager connectApplinkManager, I18nResolver i18nResolver, ConnectAddonBeanFactory connectAddonBeanFactory,
                               SharedSecretService sharedSecretService,
                               ConnectHttpClientFactory connectHttpClientFactory,
                               DarkFeatureManager darkFeatureManager,
                               ConnectAddonAccessor connectAddonAccessor)
    {
        this.isDevModeService = isDevModeService;
        this.userManager = userManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.connectAddonAccessor = connectAddonAccessor;
        this.httpClient = connectHttpClientFactory.getInstance();
        this.addonRegistry = addonRegistry;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.connectUserService = connectUserService;
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
        this.darkFeatureManager = darkFeatureManager;
    }

    public boolean hasDescriptor(String pluginKey)
    {
        return addonRegistry.hasDescriptor(pluginKey);
    }

    public Iterable<String> getAllAddonKeys()
    {
        return addonRegistry.getAllAddonKeys();
    }

    /**
     * This method is public for test visibility. In preference, please use
     * {@link ConnectAddonInstaller#install(String)}
     *
     * @param jsonDescriptor the json descriptor of the add-on to install
     * @param targetState  the intended state of the add-on after a successful installation
     * @param maybePreviousSharedSecret   optionally, the previous shared secret (used for signing)
     * @param reusePreviousPublicKeyOrSharedSecret   toggle whether or not we issue a new secret/key if the previous one is defined
     */
    @VisibleForTesting
    public void installConnectAddon(String jsonDescriptor, PluginState targetState, Optional<String> maybePreviousSharedSecret, boolean reusePreviousPublicKeyOrSharedSecret) throws ConnectAddonInstallException
    {
        long startTime = System.currentTimeMillis();

        ConnectAddonBean addon = connectAddonBeanFactory.fromJson(jsonDescriptor);
        String pluginKey = addon.getKey();
        String previousDescriptor = addonRegistry.getDescriptor(pluginKey);

        AuthenticationType newAuthType = addon.getAuthentication().getType();
        final boolean newUseSharedSecret = addonUsesSymmetricSharedSecret(newAuthType, JWT_ALGORITHM);
        String newSharedSecret = newUseSharedSecret
                ? reusePreviousPublicKeyOrSharedSecret && maybePreviousSharedSecret.isPresent()
                    ? maybePreviousSharedSecret.get()
                    : sharedSecretService.next()
                : null;
        String newAddonSigningKey = newUseSharedSecret ? newSharedSecret : addon.getAuthentication().getPublicKey(); // the key stored on the applink: used to sign outgoing requests and verify incoming requests

        String userKey = provisionUserIfNecessary(addon, previousDescriptor);

        AddonSettings settings = new AddonSettings()
                .setAuth(newAuthType.name())
                .setBaseUrl(addon.getBaseUrl())
                .setDescriptor(jsonDescriptor)
                .setRestartState(PluginState.DISABLED)
                .setUserKey(userKey);

        if (!Strings.isNullOrEmpty(newSharedSecret))
        {
            settings.setSecret(newSharedSecret);
        }

        addonRegistry.storeAddonSettings(pluginKey, settings);

        //applink MUST be created before any modules but AFTER we store the settings
        connectApplinkManager.createAppLink(addon, addon.getBaseUrl(), newAuthType, newAddonSigningKey, userKey);

        //make the sync callback if needed
        if (!Strings.isNullOrEmpty(addon.getLifecycle().getInstalled()))
        {
            if (darkFeatureManager.isFeatureEnabledForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY))
            {
                requestInstallCallback(addon, newSharedSecret, true); // sign using whatever shared secret is looked up (the old code path)
            }
            else
            {
                // TODO ACDEV-1596: Because we've got exactly one auth generator per add-on this if statement's condition
                // will cause us to NOT sign if the old descriptor used a shared secret but the new descriptor does NOT.
                if (maybePreviousSharedSecret.isPresent() && newUseSharedSecret)
                {
                    requestInstallCallback(addon, newSharedSecret, maybePreviousSharedSecret.get()); // sign using the previous shared secret
                }
                else
                {
                    requestInstallCallback(addon, newSharedSecret, false); // do not sign
                }
            }
        }

        eventPublisher.publish(new ConnectAddonInstalledEvent(pluginKey));

        long endTime = System.currentTimeMillis();
        log.info("Connect addon '" + addon.getKey() + "' installed in " + (endTime - startTime) + "ms");

        if (PluginState.ENABLED == targetState)
        {
            try
            {
                enableConnectAddon(pluginKey);
            }
            catch (ConnectAddonEnableException e)
            {
                log.error("Could not enable add-on " + e.getAddonKey() + " during its installation: " + e.getMessage(), e);
            }
        }
    }

    private static boolean addonRequiresAuth(ConnectAddonBean addon)
    {
        return addon.getAuthentication() != null && !AuthenticationType.NONE.equals(addon.getAuthentication().getType());
    }

    public String provisionUserIfNecessary(ConnectAddonBean addon, String previousDescriptor) throws ConnectAddonInstallException
    {
        return addonRequiresAuth(addon) ? provisionAddonUserAndScopes(addon, previousDescriptor) : null;
    }

    public void enableConnectAddon(final String pluginKey) throws ConnectAddonInitException, ConnectAddonEnableException
    {
        long startTime = System.currentTimeMillis();
        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
        remotablePluginAccessorFactory.remove(pluginKey);

        if (addonRegistry.hasDescriptor(pluginKey))
        {
            ConnectAddonBean addon = connectAddonAccessor.getAddon(pluginKey).get();

            if (null != addon)
            {
                try
                {
                    beanToModuleRegistrar.registerDescriptorsForBeans(addon);
                }
                catch (ConnectModuleRegistrationException e)
                {
                    eventPublisher.publish(new ConnectAddonEnableFailedEvent(pluginKey, e.getMessage()));
                    throw new ConnectAddonEnableException(pluginKey, "Module registration failed while enabling add-on, skipping.", e);
                }

                if (addonRequiresAuth(addon))
                {
                    enableAddonUser(addon);
                }

                addonRegistry.storeRestartState(pluginKey, PluginState.ENABLED);

                final ConnectAddonEnabledEvent enabledEvent = new ConnectAddonEnabledEvent(pluginKey, createEventData(pluginKey, SyncHandler.ENABLED.name().toLowerCase()));
                try
                {
                    eventPublisher.publish(enabledEvent);
                }
                catch (Exception e) {
                    log.warn(String.format("Could not fire enabled webhook event for add-on %s, continuing anyway", pluginKey), e);
                }

                long endTime = System.currentTimeMillis();
                log.info("Connect addon '" + addon.getKey() + "' enabled in " + (endTime - startTime) + "ms");
            }
            else
            {
                String message = "Tried to publish plugin enabled event for addon, but got a null ConnectAddonBean when trying to deserialize its stored descriptor. Ignoring...";
                eventPublisher.publish(new ConnectAddonEnableFailedEvent(pluginKey, message));
                throw new ConnectAddonEnableException(pluginKey, message);
            }
        }
        else
        {
            String message = "Tried to enable add-on before it was installed.";
            eventPublisher.publish(new ConnectAddonEnableFailedEvent(pluginKey, message));
            throw new ConnectAddonEnableException(pluginKey, message);
        }
    }

    public void disableConnectAddon(final String pluginKey) throws ConnectAddonDisableException
    {
        disableConnectAddon(pluginKey, true, true);
    }

    public void disableConnectAddonWithoutPersistingState(final String pluginKey)
            throws ConnectAddonDisableException
    {
        disableConnectAddon(pluginKey, false, true);
    }

    private void disableConnectAddon(final String pluginKey, boolean persistState, boolean sendEvent)
            throws ConnectAddonDisableException
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

            disableAddonUser(pluginKey);
            beanToModuleRegistrar.unregisterDescriptorsForAddon(pluginKey);

            if (persistState)
            {
                addonRegistry.storeRestartState(pluginKey, PluginState.DISABLED);
            }

            long endTime = System.currentTimeMillis();
            log.info("Connect addon '" + pluginKey + "' disabled in " + (endTime - startTime) + "ms");
        }
    }

    public void uninstallConnectAddon(final String pluginKey) throws ConnectAddonDisableException
    {
        uninstallConnectAddon(pluginKey, true);
    }

    public void uninstallConnectAddonQuietly(final String pluginKey)
    {
        try
        {
            uninstallConnectAddon(pluginKey, false);
        }
        catch (ConnectAddonDisableException e)
        {
            //uh, don't you know what "quietly" means?
        }
    }

    private void uninstallConnectAddon(final String pluginKey, boolean sendEvent)
            throws ConnectAddonDisableException
    {
        long startTime = System.currentTimeMillis();
        if (addonRegistry.hasDescriptor(pluginKey))
        {
            String descriptor = addonRegistry.getDescriptor(pluginKey);
            Optional<String> maybeSharedSecret = Optional.empty();

            try
            {
                ConnectAddonBean addon = connectAddonAccessor.getAddon(pluginKey).get();

                disableConnectAddon(pluginKey, false, sendEvent);

                if (null != addon)
                {
                    if (sendEvent && !Strings.isNullOrEmpty(addon.getLifecycle().getUninstalled()))
                    {
                        try
                        {
                            final URI callbackUri = getURI(addon.getBaseUrl(), addon.getLifecycle().getUninstalled());
                            callSyncHandler(addon.getKey(),
                                            addonUsesJwtAuthentication(addon),
                                            callbackUri,
                                            createEventDataForUninstallation(pluginKey, addon),
                                            getAuthHeader(callbackUri, remotablePluginAccessorFactory.get(addon.getKey()).getAuthorizationGenerator()));
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

                    if (addonUsesSymmetricSharedSecret(addon, JWT_ALGORITHM))
                    {
                        final ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);

                        if (null != appLink)
                        {
                            maybeSharedSecret = connectApplinkManager.getSharedSecretOrPublicKey(appLink);
                        }
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
                connectAddonBeanFactory.remove(descriptor);

                // if the add-on had a shared secret then store it so that we can sign an installed callback
                // in DefaultConnectAddonInstaller.install(java.lang.String)() if the user turns around and re-installs the add-on
                if (maybeSharedSecret.isPresent())
                {
                    AddonSettings uninstalledRemnant = new AddonSettings();
                    uninstalledRemnant.setSecret(maybeSharedSecret.get());
                    uninstalledRemnant.setRestartState(PluginState.UNINSTALLED);
                    addonRegistry.storeAddonSettings(pluginKey, uninstalledRemnant); // do it in one call for efficiency
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Connect addon '" + pluginKey + "' uninstalled in " + (endTime - startTime) + "ms");
    }

    // first install: no previous shared secret, no signing
    private void requestInstallCallback(ConnectAddonBean addon, String sharedSecret, final boolean sign) throws ConnectAddonInstallException
    {
        final URI callbackUri = getURI(addon.getBaseUrl(), addon.getLifecycle().getInstalled());
        final Optional<String> authHeader = sign ? getAuthHeader(callbackUri, remotablePluginAccessorFactory.get(addon.getKey()).getAuthorizationGenerator()) : Optional.<String>empty();
        requestInstallCallback(addon, sharedSecret, callbackUri, authHeader);
    }

    // reinstalls: sign with the previous shared secret so that the add-on can verify that the sender of the request is in possession of the previous shared secret
    private void requestInstallCallback(ConnectAddonBean addon, String sharedSecret, String previousSharedSecret) throws ConnectAddonInstallException
    {
        final URI callbackUri = getURI(addon.getBaseUrl(), addon.getLifecycle().getInstalled());
        final AuthorizationGenerator authorizationGenerator = remotablePluginAccessorFactory.get(addon.getKey()).getAuthorizationGenerator();

        // NB: check that the auth generator matches the request/non-request to sign with an arbitrary key on installation, not on every callback,
        // because signing with a previous key happens only on installation
        // (the runtime "instanceof ReKeyableAuthorizationGenerator" check is necessary because the OAuthSigningRemotablePluginAccessor is explicitly not re-keyable: it must sign with the same oauth key every time)
        if (authorizationGenerator instanceof ReKeyableAuthorizationGenerator)
        {
            String authHeader = getAuthHeader(callbackUri, (ReKeyableAuthorizationGenerator) authorizationGenerator, previousSharedSecret);
            requestInstallCallback(addon, sharedSecret, callbackUri, Optional.of(authHeader));
        }
        else
        {
            // this should never happen; if it does then it will result in a "something bad happened; talk to an admin" error message in the UI
            throw new IllegalArgumentException(String.format("Cannot sign outgoing request to %s with an arbitrary secret because the authorization generator for add-on %s is a %s, which is not a %s!",
                    callbackUri, addon.getKey(), authorizationGenerator.getClass().getSimpleName(), ReKeyableAuthorizationGenerator.class.getSimpleName()));
        }
    }

    private void requestInstallCallback(ConnectAddonBean addon, String sharedSecret, URI callbackUri, Optional<String> authHeader) throws ConnectAddonInstallException
    {
        try
        {
            callSyncHandler(addon.getKey(),
                    addonUsesJwtAuthentication(addon),
                    callbackUri,
                    createEventDataForInstallation(addon.getKey(), sharedSecret, addon),
                    authHeader);
        }
        catch (LifecycleCallbackException e)
        {
            Serializable[] params = e.getParams() != null ? e.getParams() : new Serializable[] {};
            throw new ConnectAddonInstallException(e.getMessage(), e.getI18nKey(), params);
        }
    }

    // removing the property from the app link removes the Authenticator's ability to assign a user to incoming requests
    // and as these users cannot log in anyway this reduces their possible actions to zero
    // (but don't remove the user as we need to preserve the history of their actions (e.g. audit trail, issue edited by <user>)
    private void disableAddonUser(String addonKey) throws ConnectAddonDisableException
    {
        ApplicationLink applicationLink = connectApplinkManager.getAppLink(addonKey);

        if (null != applicationLink)
        {
            applicationLink.removeProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
        }

        connectUserService.disableAddonUser(addonKey);
    }

    private void enableAddonUser(ConnectAddonBean addon) throws ConnectAddonInitException
    {
        String userKey = connectUserService.getOrCreateAddonUserName(addon.getKey(), addon.getName());

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
    private void callSyncHandler(String addonKey, final boolean addonUsesJwtAuthentication, URI callbackUri, String jsonEventData, Optional<String> authHeader) throws LifecycleCallbackException
    {
        // try distributing prod shared secrets over http (note the lack of "s") and it shall be rejected
        if (!isDevModeService.isDevMode() && addonUsesJwtAuthentication && !callbackUri.getScheme().toLowerCase().startsWith("https"))
        {
            String message = String.format("Cannot issue callback except via HTTPS. Current URL = '%s'", callbackUri);
            throw new LifecycleCallbackException(message, "connect.remote.upm.install.exception");
        }

        Response response = getSyncHandlerResponse(addonKey, callbackUri, jsonEventData, authHeader);

        final int statusCode = response.getStatusCode();
        // a selection of 2xx response codes both indicate success and are semantically valid for this callback
        if (!OK_INSTALL_HTTP_CODES.contains(statusCode))
        {
            String statusText = response.getStatusText();
            String responseEntity = response.getEntity(); // calling response.getEntity() multiple times results in IllegalStateException("Entity may only be accessed once")
            log.error("Error contacting remote application at " + callbackUri + " " + statusCode + ":[" + statusText + "]:" + responseEntity);

            String message = "Error contacting remote application " + statusCode + ":[" + statusText + "]:" + responseEntity;
            throw new LifecycleCallbackException(message, findI18nKeyForHttpErrorCode(statusCode));
        }
    }

    private static boolean addonUsesJwtAuthentication(ConnectAddonBean addon)
    {
        return null != addon.getAuthentication() && AuthenticationType.JWT.equals(addon.getAuthentication().getType());
    }

    private static Optional<String> getAuthHeader(final URI callbackUri, final AuthorizationGenerator authorizationGenerator)
    {
        return authorizationGenerator.generate(HttpMethod.POST, callbackUri, Collections.<String, String[]>emptyMap());
    }

    private static String getAuthHeader(final URI callbackUri, final ReKeyableAuthorizationGenerator authorizationGenerator, final String secret)
    {
        return authorizationGenerator.generate(HttpMethod.POST, callbackUri, Collections.<String, String[]>emptyMap(), secret);
    }

    private Response getSyncHandlerResponse(String addonKey, URI callbackUri, String jsonEventData, Optional<String> authHeader) throws LifecycleCallbackException
    {
        try
        {
            Request.Builder request = httpClient.newRequest(callbackUri);
            request.setAttribute("purpose", "web-hook-notification");
            request.setAttribute("pluginKey", addonKey);
            request.setContentType(MediaType.APPLICATION_JSON);
            request.setEntity(jsonEventData);

            if (authHeader.isPresent())
            {
                request.setHeader(AUTHORIZATION_HEADER, authHeader.get());
            }

            request.setHeader(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION, getConnectPluginVersion());

            return request.execute(Request.Method.POST).claim();
        }
        catch (Exception e)
        {
            log.error("Error contacting remote application at " + callbackUri + "  [" + e.getMessage() + "]", e);
            String message = "Error contacting remote application [" + e.getMessage() + "]";

            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException)
            {
                throw new LifecycleCallbackException(message, "connect.install.error.remote.host.bad.domain", callbackUri.getHost());
            }
            else if (cause instanceof SocketTimeoutException)
            {
                throw new LifecycleCallbackException(message, "connect.install.error.remote.host.timeout", removeQuery(callbackUri));
            }
            else if (cause instanceof SSLException)
            {
                throw new LifecycleCallbackException(message, "connect.install.error.remote.host.ssl", removeQuery(callbackUri), cause.getMessage());
            }

            throw new LifecycleCallbackException(message, "connect.remote.upm.install.exception");
        }
    }

    // we don't want to see "?user_key=2c9680504384c481014384c49e6a0004" in installation failure messages show to the users
    private String removeQuery(URI uri)
    {
        String uriString = uri.toString();
        String query = uri.getQuery();
        if (query != null)
        {
            uriString = uriString.replace(query, "");
        }
        return uriString.endsWith("?") ? uriString.substring(0, uriString.length()-1) : uriString;
    }

    private String findI18nKeyForHttpErrorCode(final int responseCode)
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

        return i18nKey;
    }

    private URI getURI(String addonBaseUrl, String endpointRelativePath)
    {
        UriBuilder builder = new UriBuilder().setPath(addonBaseUrl + endpointRelativePath);

        UserProfile user = userManager.getRemoteUser();
        if (null != user)
        {
            builder.addQueryParameter(USER_KEY, user.getUserKey().getStringValue());
        }

        return builder.toUri().toJavaUri();
    }

    @VisibleForTesting
    String createEventData(String pluginKey, String eventType)
    {
        return createEventDataInternal(pluginKey, eventType, null);
    }

    String createEventDataForInstallation(String pluginKey, String sharedSecret, ConnectAddonBean addon)
    {
        return createEventDataInternal(pluginKey, SyncHandler.INSTALLED.name().toLowerCase(), sharedSecret);
    }

    String createEventDataForUninstallation(String pluginKey, ConnectAddonBean addon)
    {
        return createEventDataInternal(pluginKey, SyncHandler.UNINSTALLED.name().toLowerCase(), null);
    }

    // NB: the sharedSecret should be distributed synchronously and only on installation
    private String createEventDataInternal(String pluginKey, String eventType, String sharedSecret)
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

        ConnectAddonEventData data = dataBuilder.build();

        return ConnectModulesGsonFactory.getGson().toJson(data);
    }

    private String getConnectPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
    }

    private boolean addonUsesSymmetricSharedSecret(ConnectAddonBean addonBean, SigningAlgorithm jwtAlgorithm)
    {
        AuthenticationBean authenticationBean = addonBean.getAuthentication();
        return null != authenticationBean && addonUsesSymmetricSharedSecret(authenticationBean.getType(), jwtAlgorithm);
    }

    // TODO ACDEV-378: also check the algorithm
    private boolean addonUsesSymmetricSharedSecret(AuthenticationType authType, SigningAlgorithm algorithm)
    {
        return AuthenticationType.JWT.equals(authType) && algorithm.requiresSharedSecret();
    }

    private String provisionAddonUserAndScopes(ConnectAddonBean addon, String previousDescriptor)
            throws PluginInstallException, ConnectAddonInstallException
    {
        Set<ScopeName> previousScopes = Sets.newHashSet();
        Set<ScopeName> newScopes = addon.getScopes();

        if (StringUtils.isNotBlank(previousDescriptor))
        {
            ConnectAddonBean previousAddon = connectAddonBeanFactory.fromJson(previousDescriptor);
            previousScopes = previousAddon.getScopes();
        }

        try
        {
            return connectUserService.provisionAddonUserWithScopes(addon, previousScopes, newScopes);
        }
        catch (ConnectAddonInitException e)
        {
            ConnectAddonInstallException exception = new ConnectAddonInstallException(e.getMessage(), e.getI18nKey(), addon.getName());
            exception.initCause(e);
            throw exception;
        }

    }
}
