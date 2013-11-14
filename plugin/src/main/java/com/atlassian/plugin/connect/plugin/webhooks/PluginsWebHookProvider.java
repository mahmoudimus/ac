package com.atlassian.plugin.connect.plugin.webhooks;

import javax.inject.Named;

import com.atlassian.plugin.connect.plugin.capabilities.event.ConnectAddonEventSerializer;
import com.atlassian.plugin.connect.spi.event.*;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.provider.*;

/**
 * Registers Web hooks for remote plugins
 */
@ExportAsService
@Named
public final class PluginsWebHookProvider implements WebHookProvider
{
    public static final String REMOTE_PLUGIN_INSTALLED = "remote_plugin_installed";
    public static final String REMOTE_PLUGIN_ENABLED = "remote_plugin_enabled";
    public static final String REMOTE_PLUGIN_DISABLED = "remote_plugin_disabled";

    public static final String CONNECT_ADDON_ENABLED = "connect_addon_enabled";
    public static final String CONNECT_ADDON_DISABLED = "connect_addon_disabled";
    public static final String CONNECT_ADDON_UNINSTALLED = "connect_addon_uninstalled";

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        /* Legacy XML Stuff */
        final EventSerializerFactory serializerFactory = new RemotePluginEventSerializerFactory();
        final RemotePluginEventMatcher eventTypeMatcher = new RemotePluginEventMatcher();

        registrar.webhook(REMOTE_PLUGIN_INSTALLED).whenFired(RemotePluginInstalledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        registrar.webhook(REMOTE_PLUGIN_ENABLED).whenFired(RemotePluginEnabledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        registrar.webhook(REMOTE_PLUGIN_DISABLED).whenFired(RemotePluginDisabledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        
        /* New Json Stuff */
        final EventSerializerFactory connectAddonEventSerializerFactory = new ConnectAddonEventSerializerFactory();
        final ConnectAddonEventMatcher connectAddonEventMatcher = new ConnectAddonEventMatcher();
        registrar.webhook(CONNECT_ADDON_ENABLED).whenFired(ConnectAddonEnabledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
        registrar.webhook(CONNECT_ADDON_DISABLED).whenFired(ConnectAddonDisabledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
        //registrar.webhook(CONNECT_ADDON_UNINSTALLED).whenFired(ConnectAddonUninstalledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
    }

    private static final class RemotePluginEventMatcher<E extends RemotePluginEvent> implements EventMatcher<E>
    {
        @Override
        public boolean matches(final E event, final Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) consumerParams).getPluginKey().equals(event.getPluginKey());
        }
    }

    private static final class RemotePluginEventSerializerFactory<E extends RemotePluginEvent> implements EventSerializerFactory<E>
    {
        @Override
        public EventSerializer create(final E event)
        {
            return EventSerializers.forMap(event, event.toMap());
        }
    }

    private static final class ConnectAddonEventMatcher<E extends ConnectAddonEvent> implements EventMatcher<E>
    {
        @Override
        public boolean matches(final E event, final Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) consumerParams).getPluginKey().equals(event.getPluginKey());
        }
    }

    private static final class ConnectAddonEventSerializerFactory<E extends ConnectAddonEvent> implements EventSerializerFactory<E>
    {
        @Override
        public EventSerializer create(final E event)
        {
            return new ConnectAddonEventSerializer(event,event.getData());
        }
    }
}
