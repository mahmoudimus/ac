package com.atlassian.plugin.remotable.plugin.webhook;

import com.atlassian.plugin.remotable.spi.webhook.EventMatcher;
import com.atlassian.plugin.remotable.spi.webhook.EventSerializer;

import java.net.URI;

public interface WebHookPublisher
{
    void register(String pluginKey, String eventIdentifier, URI path);

    void unregister(String pluginKey, String eventIdentifier, URI url);

    void publish(String eventIdentifier, EventMatcher<Object> eventMatcher, EventSerializer eventSerializer);
}
