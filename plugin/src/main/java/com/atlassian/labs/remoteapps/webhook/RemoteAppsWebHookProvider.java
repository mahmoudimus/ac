package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.labs.remoteapps.event.*;
import com.atlassian.labs.remoteapps.webhook.external.*;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Web hooks for the remote apps plugin itself
 */
@Component
public class RemoteAppsWebHookProvider implements WebHookProvider
{
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;
    private static final EventMatcher<RemoteAppEvent> matcher = new SameAppMatcher();

    @Autowired
    public RemoteAppsWebHookProvider(ApplicationProperties applicationProperties,
            ConsumerService consumerService)
    {
        this.applicationProperties = applicationProperties;
        this.consumerService = consumerService;
    }

    @Override
    public void provide(WebHookRegistrar publish)
    {
        final String baseUrl = applicationProperties.getBaseUrl();
        EventSerializerFactory factory = new EventSerializerFactory<RemoteAppEvent>()
        {
            @Override
            public EventSerializer create(RemoteAppEvent event)
            {
                return new MapEventSerializer(event, ImmutableMap.<String, Object>of(
                                "key", event.getRemoteAppKey(),
                                "serverKey", consumerService.getConsumer().getKey(),
                                "baseurl", (baseUrl != null ? baseUrl : ""),
                                "baseUrl", (baseUrl != null ? baseUrl : "")));
            }
        };

        publish.webhook("remote_app_installed").whenFired(RemoteAppInstalledEvent.class)
                .matchedBy(matcher)
                .serializedWith(factory);
        publish.webhook("remote_app_started").whenFired(RemoteAppStartedEvent.class)
                .matchedBy(matcher)
                .serializedWith(factory);
    }
    
    private static class SameAppMatcher implements EventMatcher<RemoteAppEvent>
    {
        @Override
        public boolean matches(RemoteAppEvent event, String pluginKey)
        {
            return event.getRemoteAppKey().equals(pluginKey);
        }
    }
}
