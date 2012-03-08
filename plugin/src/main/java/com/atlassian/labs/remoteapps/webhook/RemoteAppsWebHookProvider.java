package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.labs.remoteapps.event.RemoteAppEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializerFactory;
import com.atlassian.labs.remoteapps.webhook.external.WebHookProvider;
import com.atlassian.labs.remoteapps.webhook.external.WebHookRegistrar;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

/**
 * Web hooks for the remote apps plugin itself
 */
@Component
public class RemoteAppsWebHookProvider implements WebHookProvider
{
    @Override
    public void provide(WebHookRegistrar publish)
    {
        EventSerializerFactory factory = new EventSerializerFactory<RemoteAppEvent>()
        {
            @Override
            public EventSerializer create(RemoteAppEvent event)
            {
                return new MapEventSerializer(event, ImmutableMap.<String, Object>of(
                                "key", event.getRemoteAppKey()));
            }
        };

        publish.webhook("remote_app_installed").whenFired(RemoteAppInstalledEvent.class)
                .serializedWith(factory);
        publish.webhook("remote_app_started").whenFired(RemoteAppStartedEvent.class)
                .serializedWith(factory);
    }
}
