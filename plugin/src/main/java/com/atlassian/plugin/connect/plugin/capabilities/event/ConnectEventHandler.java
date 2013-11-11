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
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonEnabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonUninstalledEvent;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.sal.api.ApplicationProperties;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.google.common.base.Strings.nullToEmpty;

@Named
public class ConnectEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectEventHandler.class);
    private final EventPublisher eventPublisher;
    private final PluginEventManager pluginEventManager;
    private final PluginAccessor pluginAccessor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
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

    @Inject
    public ConnectEventHandler(EventPublisher eventPublisher, PluginEventManager pluginEventManager, PluginAccessor pluginAccessor, RemotablePluginAccessorFactory pluginAccessorFactory, UserManager userManager, HttpClient httpClient, RequestSigner requestSigner, ConsumerService consumerService, ApplicationProperties applicationProperties, ProductAccessor productAccessor, BundleContext bundleContext, JsonConnectAddOnIdentifierService connectIdentifier, ConnectDescriptorRegistry descriptorRegistry, BeanToModuleRegistrar beanToModuleRegistrar)
    {
        this.eventPublisher = eventPublisher;
        this.pluginEventManager = pluginEventManager;
        this.pluginAccessor = pluginAccessor;
        this.pluginAccessorFactory = pluginAccessorFactory;
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
            callSyncHandler(addon);
        }
    }

    @PluginEventListener
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
        }
    }

    @PluginEventListener
    public void pluginDisabled(BeforePluginDisabledEvent pluginDisabledEvent)
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            beanToModuleRegistrar.unregisterDescriptorsForPlugin(plugin);
            eventPublisher.publish(new ConnectAddonDisabledEvent(plugin.getKey(), createEventData(plugin.getKey())));
        }
    }

    @PluginEventListener
    public void pluginUninstalled(PluginUninstalledEvent pluginUninstalledEvent)
    {
        final Plugin plugin = pluginUninstalledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            //just in case
            beanToModuleRegistrar.unregisterDescriptorsForPlugin(plugin);

            eventPublisher.publish(new ConnectAddonUninstalledEvent(plugin.getKey(), createEventData(plugin.getKey())));
        }
    }

    public void publishEnabledEvent(String pluginKey)
    {
        eventPublisher.publish(new ConnectAddonEnabledEvent(pluginKey, createEventData(pluginKey)));
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

    private void callSyncHandler(ConnectAddonBean addon)
    {
        Option<String> errorI18nKey = Option.<String>some("connect.remote.upm.install.exception");
        try
        {
            String pluginKey = addon.getKey();
            String callbackUrl = addon.getBaseUrl() + addon.getLifecycle().getInstalled();

            String json = createEventData(pluginKey);

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
                log.error("Error contacting remote application [" + response.getStatusText() + "]");
                throw new PluginInstallException("Error contacting remote application [" + response.getStatusText() + "]", errorI18nKey);
            }

        }
        catch (Exception e)
        {
            log.error("Error contacting remote application [" + e.getMessage() + "]", e);
            throw new PluginInstallException("Error contacting remote application [" + e.getMessage() + "]", errorI18nKey);
        }
    }

    @VisibleForTesting
    String createEventData(String pluginKey)
    {
        final Consumer consumer = consumerService.getConsumer();

        ConnectAddonEventDataBuilder dataBuilder = newConnectAddonEventData();
        dataBuilder.withBaseUrl(nullToEmpty(applicationProperties.getBaseUrl()))
                   .withPluginKey(pluginKey)
                   .withClientKey(nullToEmpty(consumer.getKey()))
                   .withPublicKey(nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                   .withPluginsVersion(nullToEmpty(getConnectPluginVersion()))
                   .withServerVersion(nullToEmpty(applicationProperties.getBuildNumber()))
                   .withProductType(nullToEmpty(productAccessor.getKey()))
                   .withDescription(nullToEmpty(consumer.getDescription()))
                   .withLink("oauth", applicationProperties.getBaseUrl() + "/rest/atlassian-connect/latest/oauth");

        UserProfile user = userManager.getRemoteUser();
        if (null != user)
        {
            dataBuilder.withUserId(user.getUsername())
                       .withUserKey(user.getUserKey().getStringValue());
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
            builder.addQueryParameter("user_id", user.getUsername())
                   .addQueryParameter("user_key", user.getUserKey().getStringValue());
        }

        return builder.toUri().toJavaUri();
    }

    private String getConnectPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
    }
}
