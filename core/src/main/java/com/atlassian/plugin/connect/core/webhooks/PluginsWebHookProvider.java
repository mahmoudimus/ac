package com.atlassian.plugin.connect.core.webhooks;

import com.atlassian.plugin.connect.core.capabilities.event.ConnectAddonEventSerializer;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonEnabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonLifecycleEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonLifecycleWithDataEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

import javax.inject.Named;

/**
 * Registers Web hooks for remote plugins
 */
@ExportAsService
@Named
public final class PluginsWebHookProvider implements WebHookProvider
{
    public static final String CONNECT_ADDON_ENABLED = "connect_addon_enabled";
    public static final String CONNECT_ADDON_DISABLED = "connect_addon_disabled";
    public static final String CONNECT_ADDON_UNINSTALLED = "connect_addon_uninstalled";

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        final EventSerializerFactory connectAddonEventSerializerFactory = new ConnectAddonEventSerializerFactory();
        final ConnectAddonEventMatcher connectAddonEventMatcher = new ConnectAddonEventMatcher();
        registrar.webhook(CONNECT_ADDON_ENABLED).whenFired(ConnectAddonEnabledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
        registrar.webhook(CONNECT_ADDON_DISABLED).whenFired(ConnectAddonDisabledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
        //registrar.webhook(CONNECT_ADDON_UNINSTALLED).whenFired(ConnectAddonUninstalledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
    }

    private static final class ConnectAddonEventMatcher<E extends ConnectAddonLifecycleEvent> implements EventMatcher<E>
    {
        @Override
        public boolean matches(final E event, final Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) consumerParams).getPluginKey().equals(event.getPluginKey());
        }
    }

    private static final class ConnectAddonEventSerializerFactory<E extends ConnectAddonLifecycleEvent> implements EventSerializerFactory<E>
    {
        @Override
        public EventSerializer create(final E event)
        {
            String data = null;
            if (event instanceof ConnectAddonLifecycleWithDataEvent)
            {
                data = ((ConnectAddonLifecycleWithDataEvent) event).getData();
            }
            return new ConnectAddonEventSerializer(event, data);
        }
    }

}
