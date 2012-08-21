package com.atlassian.labs.remoteapps.container.service;

import com.atlassian.event.api.EventPublisher;

/**
 * A no-op event publisher impl for runing in the container.
 */
public class ContainerEventPublisher implements EventPublisher
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
