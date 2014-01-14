package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonEnabledEvent;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import java.net.URI;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

@Named
public class ConnectEventHandler implements InitializingBean, DisposableBean
{
    public static final String INSTALLED = "installed";
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    public static final String UNINSTALLED = "uninstalled";

    private static final Logger log = LoggerFactory.getLogger(ConnectEventHandler.class);
    public static final String USER_KEY = "user_key";

    private final EventPublisher eventPublisher;
    private final PluginEventManager pluginEventManager;
    private final UserManager userManager;
    private final HttpClient httpClient;
    private final RequestSigner requestSigner;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;
    private final ProductAccessor productAccessor;
    private final BundleContext bundleContext;
    private final JsonConnectAddOnIdentifierService connectIdentifier;
    private final ConnectDescriptorRegistry descriptorRegistry;
    private final BeanToModuleRegistrar beanToModuleRegistrar;
    private final LicenseRetriever licenseRetriever;
    private final IsDevModeService isDevModeService;

    @Inject
    public ConnectEventHandler(EventPublisher eventPublisher,
                               PluginEventManager pluginEventManager,
                               UserManager userManager,
                               HttpClient httpClient,
                               RequestSigner requestSigner,
                               ConsumerService consumerService,
                               ApplicationProperties applicationProperties,
                               ProductAccessor productAccessor,
                               BundleContext bundleContext,
                               JsonConnectAddOnIdentifierService connectIdentifier,
                               ConnectDescriptorRegistry descriptorRegistry,
                               BeanToModuleRegistrar beanToModuleRegistrar,
                               LicenseRetriever licenseRetriever,
                               IsDevModeService devModeService)
    {
        this.eventPublisher = eventPublisher;
        this.pluginEventManager = pluginEventManager;
        this.licenseRetriever = licenseRetriever;
        this.userManager = userManager;
        this.httpClient = httpClient;
        this.requestSigner = requestSigner;
        this.consumerService = consumerService;
        this.applicationProperties = applicationProperties;
        this.productAccessor = productAccessor;
        this.bundleContext = bundleContext;
        this.connectIdentifier = connectIdentifier;
        this.descriptorRegistry = descriptorRegistry;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.isDevModeService = devModeService;
    }

    public void pluginInstalled(ConnectAddonBean addon, String sharedSecret)
    {
        if (!Strings.isNullOrEmpty(addon.getLifecycle().getInstalled()))
        {
            callSyncHandler(addon, addon.getLifecycle().getInstalled(), createEventDataForInstallation(addon.getKey(), sharedSecret, addon));
        }
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent)
    {
        final Plugin plugin = pluginEnabledEvent.getPlugin();
        String pluginKey = plugin.getKey();

        //if a descriptor is not stored, it means this event was fired during install before modules were created and we need to ignore
        if (connectIdentifier.isConnectAddOn(plugin) && descriptorRegistry.hasDescriptor(pluginKey))
        {
            ConnectAddonBean addon = ConnectModulesGsonFactory.getGson().fromJson(descriptorRegistry.getDescriptor(pluginKey), ConnectAddonBean.class);

            if (null != addon)
            {
                beanToModuleRegistrar.registerDescriptorsForBeans(plugin, addon);
                publishEnabledEvent(pluginKey);
            }
            else
            {
                log.warn("Tried to publish plugin enabled event for connect addon ['" + pluginKey + "'], but got a null ConnectAddonBean when trying to deserialize it's stored descriptor. Ignoring...");
            }
        }
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginDisabled(BeforePluginDisabledEvent pluginDisabledEvent)
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            eventPublisher.publish(new ConnectAddonDisabledEvent(plugin.getKey(), createEventData(plugin.getKey(), DISABLED)));
        }
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginDisabled(PluginDisabledEvent pluginDisabledEvent)
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            beanToModuleRegistrar.unregisterDescriptorsForPlugin(plugin);
        }
    }

    @PluginEventListener
    @SuppressWarnings ("unused")
    public void pluginUninstalled(PluginUninstalledEvent pluginUninstalledEvent)
    {
        final Plugin plugin = pluginUninstalledEvent.getPlugin();
        String pluginKey = plugin.getKey();
        if (descriptorRegistry.hasDescriptor(pluginKey))
        {
            ConnectAddonBean addon = ConnectModulesGsonFactory.getGson().fromJson(descriptorRegistry.getDescriptor(pluginKey), ConnectAddonBean.class);

            if (null != addon)
            {
                if (!Strings.isNullOrEmpty(addon.getLifecycle().getUninstalled()))
                {
                    try
                    {
                        callSyncHandler(addon, addon.getLifecycle().getUninstalled(), createEventData(pluginKey, UNINSTALLED));
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

            descriptorRegistry.removeDescriptor(pluginKey);
        }
    }

    public void publishEnabledEvent(String pluginKey)
    {
        eventPublisher.publish(new ConnectAddonEnabledEvent(pluginKey, createEventData(pluginKey, ENABLED)));
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
    private void callSyncHandler(ConnectAddonBean addon, String path, String jsonEventData) throws PluginInstallException
    {
        Option<String> errorI18nKey = Option.some("connect.remote.upm.install.exception");
        String callbackUrl = addon.getBaseUrl() + path;

        // try distributing prod shared secrets over http (note the lack of "s") and it shall be rejected
        if (!isDevModeService.isDevMode() && null != addon.getAuthentication() && AuthenticationType.JWT.equals(addon.getAuthentication().getType()) && !callbackUrl.toLowerCase().startsWith("https"))
        {
            throw new PluginInstallException(String.format("Cannot issue install callback except via HTTPS. Current base URL = '%s'", addon.getBaseUrl()));
        }

        try
        {
            String pluginKey = addon.getKey();

            URI installHandler = getURI(callbackUrl);

            Request.Builder request = httpClient.newRequest(installHandler);
            request.setAttribute("purpose", "web-hook-notification");
            request.setAttribute("pluginKey", pluginKey);
            request.setContentType(MediaType.APPLICATION_JSON);
            request.setEntity(jsonEventData);

            //TODO: is there a better way to sign this?
            requestSigner.sign(installHandler, pluginKey, request);

            Response response = request.execute(Request.Method.POST).claim();
            int statusCode = response.getStatusCode();
            if (statusCode != 200 && statusCode != 204)
            {
                String statusText = response.getStatusText();
                log.error("Error contacting remote application at " + callbackUrl + " " + statusCode + ":[" + statusText + "]");
                throw new PluginInstallException("Error contacting remote application " + statusCode + ":[" + statusText + "]", errorI18nKey);
            }

        }
        catch (Exception e)
        {
            log.error("Error contacting remote application at " + callbackUrl + "  [" + e.getMessage() + "]", e);
            throw new PluginInstallException("Error contacting remote application [" + e.getMessage() + "]", errorI18nKey);
        }
    }

    @VisibleForTesting
    String createEventData(String pluginKey, String eventType)
    {
        return createEventDataInternal(pluginKey, eventType, null, null);
    }

    String createEventDataForInstallation(String pluginKey, String sharedSecret, ConnectAddonBean addon)
    {
        return createEventDataInternal(pluginKey, INSTALLED, sharedSecret, addon);
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
