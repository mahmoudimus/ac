package com.atlassian.labs.remoteapps.webhook.external;

import com.atlassian.labs.remoteapps.webhook.EventSerializer;

/**
 * Creates event serializers for an event type
 */
public interface EventSerializerFactory<T>
{
    EventSerializer create(T event);
}
