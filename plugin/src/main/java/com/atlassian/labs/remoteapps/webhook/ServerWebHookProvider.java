package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.labs.remoteapps.event.product.ServerUpgradedEvent;
import com.atlassian.labs.remoteapps.event.product.RemoteAppsUpgradedEvent;
import com.atlassian.labs.remoteapps.event.product.UpgradedEvent;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializer;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializerFactory;
import com.atlassian.labs.remoteapps.webhook.external.WebHookProvider;
import com.atlassian.labs.remoteapps.webhook.external.WebHookRegistrar;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Publishes server and remote apps plugin events as web hooks
 */
@Component
public class ServerWebHookProvider implements WebHookProvider
{
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;

    @Autowired
    public ServerWebHookProvider(ApplicationProperties applicationProperties,
            ConsumerService consumerService)
    {
        this.applicationProperties = applicationProperties;
        this.consumerService = consumerService;
    }

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        EventSerializerFactory upgradeFactory = new EventSerializerFactory<UpgradedEvent>()
        {
            @Override
            public EventSerializer create(final UpgradedEvent event)
            {
                return new MapEventSerializer(event, new HashMap<String,Object>() {{
                        put("key", consumerService.getConsumer().getKey());
                        put("baseUrl", applicationProperties.getBaseUrl());
                        put("oldVersion", event.getOldVersion());
                        put("newVersion", event.getNewVersion());
                }});
            }
        };
        registrar.webhook("server_upgraded").whenFired(ServerUpgradedEvent.class).serializedWith(upgradeFactory);

        registrar.webhook("remoteapps_upgraded").whenFired(RemoteAppsUpgradedEvent.class).serializedWith(upgradeFactory);
    }
}
