package com.atlassian.plugin.connect.plugin.event;

import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.InstallationMode;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginDisabledEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginEnabledEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginInstalledEvent;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

@Component
public final class RemoteEventsHandler implements InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final PluginEventManager pluginEventManager;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;
    private final ProductAccessor productAccessor;
    private final BundleContext bundleContext;
    private final ConnectAddOnIdentifierService connectIdentifier;

    @Autowired
    public RemoteEventsHandler(EventPublisher eventPublisher,
                               ConsumerService consumerService,
                               ApplicationProperties applicationProperties,
                               ProductAccessor productAccessor,
                               BundleContext bundleContext,
                               PluginEventManager pluginEventManager,
                               ConnectAddOnIdentifierService connectIdentifier)
    {
        this.consumerService = checkNotNull(consumerService);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.pluginEventManager = checkNotNull(pluginEventManager);
        this.productAccessor = checkNotNull(productAccessor);
        this.bundleContext = checkNotNull(bundleContext);
        this.connectIdentifier = checkNotNull(connectIdentifier);
    }

    public void pluginInstalled(String pluginKey)
    {
        eventPublisher.publish(new RemotePluginInstalledEvent(checkNotNull(pluginKey), newRemotePluginEventData()));
    }

    @PluginEventListener
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent)
    {
        final Plugin plugin = pluginEnabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            eventPublisher.publish(new RemotePluginEnabledEvent(plugin.getKey(), newRemotePluginEventData()));
        }
    }

    @PluginEventListener
    public void pluginDisabled(BeforePluginDisabledEvent pluginDisabledEvent)
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();
        if (connectIdentifier.isConnectAddOn(plugin))
        {
            eventPublisher.publish(new RemotePluginDisabledEvent(plugin.getKey(), newRemotePluginEventData()));
        }
    }

    @VisibleForTesting
    Map<String, Object> newRemotePluginEventData()
    {
        final Consumer consumer = consumerService.getConsumer();

        return ImmutableMap.<String, Object>builder()
                .put("links", ImmutableMap.of(
                        "oauth", applicationProperties.getBaseUrl() + "/rest/atlassian-connect/latest/oauth"))
                .put("clientKey", nullToEmpty(consumer.getKey()))
                .put("publicKey", nullToEmpty(RSAKeys.toPemEncoding(consumer.getPublicKey())))
                .put("serverVersion", nullToEmpty(applicationProperties.getBuildNumber()))
                .put("pluginsVersion", nullToEmpty(getRemotablePluginsPluginVersion()))
                .put("baseUrl", nullToEmpty(applicationProperties.getBaseUrl()))
                .put("productType", nullToEmpty(productAccessor.getKey()))
                .put("description", nullToEmpty(consumer.getDescription()))
                .build();
    }

    private String getRemotablePluginsPluginVersion()
    {
        Object bundleVersion = bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return bundleVersion == null ? null : bundleVersion.toString();
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
}
