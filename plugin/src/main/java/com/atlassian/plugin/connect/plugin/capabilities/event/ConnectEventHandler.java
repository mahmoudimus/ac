package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

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
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
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
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.RequestSigner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonEventData.newConnectAddonEventData;
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
            LicenseRetriever licenseRetriever)
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
    }

    public void pluginInstalled(ConnectAddonBean addon)
    {
        if (!Strings.isNullOrEmpty(addon.getLifecycle().getInstalled()))
        {
            callSyncHandler(addon, addon.getLifecycle().getInstalled(), INSTALLED);
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
            ConnectAddonBean addon = CapabilitiesGsonFactory.getGson().fromJson(descriptorRegistry.getDescriptor(pluginKey), ConnectAddonBean.class);

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
            ConnectAddonBean addon = CapabilitiesGsonFactory.getGson().fromJson(descriptorRegistry.getDescriptor(pluginKey), ConnectAddonBean.class);

            if (null != addon)
            {
                if (!Strings.isNullOrEmpty(addon.getLifecycle().getUninstalled()))
                {
                    try
                    {
                        callSyncHandler(addon, addon.getLifecycle().getUninstalled(), UNINSTALLED);
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

    private void callSyncHandler(ConnectAddonBean addon, String path, String eventType) throws PluginInstallException
    {
        Option<String> errorI18nKey = Option.some("connect.remote.upm.install.exception");
        String callbackUrl = addon.getBaseUrl() + path;
        try
        {
            String pluginKey = addon.getKey();
            String json = createEventData(pluginKey, eventType);

            URI installHandler = getURI(callbackUrl);

            Request.Builder request = httpClient.newRequest(installHandler);
            request.setAttribute("purpose", "web-hook-notification");
            request.setAttribute("pluginKey", pluginKey);
            request.setContentType(MediaType.APPLICATION_JSON);
            request.setEntity(json);

            //TODO: is there a better way to sign this?
            requestSigner.sign(installHandler, pluginKey, request);

            Response response = request.execute(Request.Method.POST).claim();
            if (response.getStatusCode() != 200)
            {
                log.error("Error contacting remote application at " + callbackUrl + " [" + response.getStatusText() + "]");
                throw new PluginInstallException("Error contacting remote application [" + response.getStatusText() + "]", errorI18nKey);
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
        final Consumer consumer = consumerService.getConsumer();

        ConnectAddonEventDataBuilder dataBuilder = newConnectAddonEventData();
        String baseUrl = applicationProperties.getBaseUrl(UrlMode.CANONICAL);

        dataBuilder.withBaseUrl(nullToEmpty(baseUrl))
                .withPluginKey(pluginKey)
                .withClientKey(nullToEmpty(consumer.getKey()))
                .withPublicKey(nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                .withPluginsVersion(nullToEmpty(getConnectPluginVersion()))
                .withServerVersion(nullToEmpty(applicationProperties.getBuildNumber()))
                .withServiceEntitlementNumber(nullToEmpty(licenseRetriever.getServiceEntitlementNumber(pluginKey)))
                .withProductType(nullToEmpty(productAccessor.getKey()))
                .withDescription(nullToEmpty(consumer.getDescription()))
                .withEventType(eventType)
                .withLink("oauth", baseUrl + "/rest/atlassian-connect/latest/oauth");

        UserProfile user = userManager.getRemoteUser();
        if (null != user)
        {
            dataBuilder.withUserKey(user.getUserKey().getStringValue());
        }

        ConnectAddonEventData data = dataBuilder.build();

        return CapabilitiesGsonFactory.getGsonBuilder().setPrettyPrinting().create().toJson(data);
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
