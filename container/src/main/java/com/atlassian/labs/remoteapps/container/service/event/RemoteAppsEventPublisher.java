package com.atlassian.labs.remoteapps.container.service.event;

import com.atlassian.event.api.EventPublisher;

/**
 * The event service for remote apps, no-op
 */
public final class RemoteAppsEventPublisher implements EventPublisher
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
