package com.atlassian.labs.remoteapps.webhook.external;

/**
 * Creates event serializers for an event type
 */
public interface EventSerializerFactory<T>
{
    EventSerializer create(T event);
}
