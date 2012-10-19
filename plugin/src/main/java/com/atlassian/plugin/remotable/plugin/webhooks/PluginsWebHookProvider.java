package com.atlassian.plugin.remotable.plugin.webhooks;

import com.atlassian.plugin.remotable.spi.event.RemotePluginInstalledEvent;
import com.atlassian.webhooks.spi.provider.*;

/**
 * Registers Web hooks for remote plugins
 */
public final class PluginsWebHookProvider implements WebHookProvider
{
    @Override
    public void provide(WebHookRegistrar registrar)
    {
        final EventSerializerFactory factory = new EventSerializerFactory<RemotePluginInstalledEvent>()
        {
            @Override
            public EventSerializer create(final RemotePluginInstalledEvent event)
            {
                return EventSerializers.forMap(event, event.toMap());
            }
        };

        registrar.webhook("remote_plugin_installed").whenFired(RemotePluginInstalledEvent.class).matchedBy(
                new EventMatcher<RemotePluginInstalledEvent>()
                {
                    @Override
                    public boolean matches(RemotePluginInstalledEvent event, String pluginKey)
                    {
                        return pluginKey.equals(event.getPluginKey());
                    }
                }
        ).serializedWith(factory);
    }
}
