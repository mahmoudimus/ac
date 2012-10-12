package com.atlassian.plugin.remotable.plugin.webhooks;

import com.atlassian.plugin.remotable.spi.event.product.PluginsUpgradedEvent;
import com.atlassian.plugin.remotable.spi.event.product.ServerUpgradedEvent;
import com.atlassian.plugin.remotable.spi.event.product.UpgradedEvent;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.EventSerializers;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

import static com.google.common.base.Preconditions.*;

/**
 * Registers Web hooks relative to:
 * <ul>
 *     <li>the host server (eg. server upgrades, etc.)</li>
 *     <li>the remotable plugins plugin (eg. remotable plugins plugin upgraded, etc.)</li>
 * </ul>
 */
public final class ServerWebHookProvider implements WebHookProvider
{
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;

    @Autowired
    public ServerWebHookProvider(ApplicationProperties applicationProperties, ConsumerService consumerService)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
        this.consumerService = checkNotNull(consumerService);
    }

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        final String baseUrl = applicationProperties.getBaseUrl();
        final EventSerializerFactory upgradeFactory = new EventSerializerFactory<UpgradedEvent>()
        {
            @Override
            public EventSerializer create(final UpgradedEvent event)
            {
                return EventSerializers.forMap(event, new HashMap<String, Object>()
                {{
                        put("key", consumerService.getConsumer().getKey());
                        put("baseUrl", (baseUrl != null ? baseUrl : ""));
                        put("oldVersion", event.getOldVersion());
                        put("newVersion", event.getNewVersion());
                    }});
            }
        };

        registrar.webhook("server_upgraded").whenFired(ServerUpgradedEvent.class).serializedWith(upgradeFactory);
        registrar.webhook("plugins_upgraded").whenFired(PluginsUpgradedEvent.class).serializedWith(upgradeFactory);
    }
}
