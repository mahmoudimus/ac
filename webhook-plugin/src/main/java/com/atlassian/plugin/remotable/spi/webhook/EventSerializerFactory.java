package com.atlassian.plugin.remotable.spi.webhook;

/**
 * Creates event serializers for an event type
 */
public interface EventSerializerFactory<T>
{
    EventSerializer create(T event);
}
