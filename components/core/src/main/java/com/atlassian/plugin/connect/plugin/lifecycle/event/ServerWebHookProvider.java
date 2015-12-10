package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.lifecycle.event.PluginsUpgradedEvent;
import com.atlassian.plugin.connect.plugin.lifecycle.event.ServerUpgradedEvent;
import com.atlassian.plugin.connect.plugin.lifecycle.event.UpgradedEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.EventSerializers;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Registers Web hooks relative to:
 * <ul>
 * <li>the host server (eg. server upgrades, etc.)</li>
 * <li>the remotable plugins plugin (eg. remotable plugins plugin upgraded, etc.)</li>
 * </ul>
 */
@ExportAsService
@Named
public final class ServerWebHookProvider implements WebHookProvider
{
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;

    @Inject
    public ServerWebHookProvider(ApplicationProperties applicationProperties, ConsumerService consumerService)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
        this.consumerService = checkNotNull(consumerService);
    }

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        final EventSerializerFactory upgradeFactory = new EventSerializerFactory<UpgradedEvent>()
        {
            @Override
            public EventSerializer create(final UpgradedEvent event)
            {
                return EventSerializers.forMap(event, new HashMap<String, Object>()
                {{
                        put("key", consumerService.getConsumer().getKey());
                        put("baseUrl", nullToEmpty(applicationProperties.getBaseUrl()));
                        put("oldVersion", event.getOldVersion());
                        put("newVersion", event.getNewVersion());
                    }});
            }
        };

        registrar.webhook("server_upgraded").whenFired(ServerUpgradedEvent.class).serializedWith(upgradeFactory);
        registrar.webhook("plugins_upgraded").whenFired(PluginsUpgradedEvent.class).serializedWith(upgradeFactory);
    }
}
