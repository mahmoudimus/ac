package com.atlassian.plugin.remotable.container.service.event;

import com.atlassian.event.api.EventPublisher;

/**
 * The event service for the container, no-op
 */
public final class ContainerEventPublisher implements EventPublisher
{
    @Override
    public void publish(Object event)
    {
    }

    @Override
    public void register(Object listener)
    {
    }

    @Override
    public void unregister(Object listener)
    {
    }

    @Override
    public void unregisterAll()
    {
    }
}
