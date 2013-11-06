package com.atlassian.plugin.connect.plugin.webhooks;

import javax.inject.Named;

import com.atlassian.plugin.connect.spi.event.RemotePluginDisabledEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginEnabledEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginInstalledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.provider.*;

/**
 * Registers Web hooks for remote plugins
 */
@ExportAsService(PluginsWebHookProvider.class)
@Named
public final class PluginsWebHookProvider implements WebHookProvider
{
    public static final String REMOTE_PLUGIN_INSTALLED = "remote_plugin_installed";
    public static final String REMOTE_PLUGIN_ENABLED = "remote_plugin_enabled";
    public static final String REMOTE_PLUGIN_DISABLED = "remote_plugin_disabled";

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        final EventSerializerFactory serializerFactory = new RemotePluginEventSerializerFactory();
        final RemotePluginEventMatcher eventTypeMatcher = new RemotePluginEventMatcher();

        registrar.webhook(REMOTE_PLUGIN_INSTALLED).whenFired(RemotePluginInstalledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        registrar.webhook(REMOTE_PLUGIN_ENABLED).whenFired(RemotePluginEnabledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        registrar.webhook(REMOTE_PLUGIN_DISABLED).whenFired(RemotePluginDisabledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
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
}
