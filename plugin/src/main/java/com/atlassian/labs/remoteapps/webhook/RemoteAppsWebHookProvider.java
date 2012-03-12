package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.event.RemoteAppEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.webhook.external.*;
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
    private static final EventMatcher<RemoteAppEvent> matcher = new SameAppMatcher();

    @Autowired
    public RemoteAppsWebHookProvider(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void provide(WebHookRegistrar publish)
    {
        EventSerializerFactory factory = new EventSerializerFactory<RemoteAppEvent>()
        {
            @Override
            public EventSerializer create(RemoteAppEvent event)
            {
                return new MapEventSerializer(event, ImmutableMap.<String, Object>of(
                                "key", event.getRemoteAppKey(),
                                "baseurl", applicationProperties.getBaseUrl()));
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
        public boolean matches(RemoteAppEvent event, ApplicationLink appLink)
        {
            return event.getRemoteAppKey().equals(
                    ((RemoteAppApplicationType)appLink.getType()).getId().get());
        }
    }
}
