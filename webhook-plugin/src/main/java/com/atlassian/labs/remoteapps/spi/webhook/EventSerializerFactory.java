package com.atlassian.labs.remoteapps.spi.webhook;

import com.atlassian.labs.remoteapps.spi.webhook.EventSerializer;

/**
 * Creates event serializers for an event type
 */
public interface EventSerializerFactory<T>
{
    EventSerializer create(T event);
}
