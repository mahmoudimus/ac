package com.atlassian.plugin.remotable.plugin.webhooks;

import com.atlassian.plugin.remotable.spi.event.RemotePluginDisabledEvent;
import com.atlassian.plugin.remotable.spi.event.RemotePluginEnabledEvent;
import com.atlassian.plugin.remotable.spi.event.RemotePluginEvent;
import com.atlassian.plugin.remotable.spi.event.RemotePluginInstalledEvent;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.EventSerializers;
import com.atlassian.webhooks.spi.provider.PluginModuleConsumerParams;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

/**
 * Registers Web hooks for remote plugins
 */
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
            return consumerParams instanceof PluginModuleConsumerParams
                    && ((PluginModuleConsumerParams) consumerParams).getPluginKey().equals(event.getPluginKey());
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
