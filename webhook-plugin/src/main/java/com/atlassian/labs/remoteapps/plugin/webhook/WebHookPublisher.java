package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.labs.remoteapps.spi.webhook.EventMatcher;
import com.atlassian.labs.remoteapps.spi.webhook.EventSerializer;

import java.net.URI;

public interface WebHookPublisher
{
    void register(String pluginKey, String eventIdentifier, URI path);

    void unregister(String pluginKey, String eventIdentifier, URI url);

    void publish(String eventIdentifier, EventMatcher<Object> eventMatcher, EventSerializer eventSerializer);
}
