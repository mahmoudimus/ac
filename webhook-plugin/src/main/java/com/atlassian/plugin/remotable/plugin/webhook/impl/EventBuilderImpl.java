package com.atlassian.plugin.remotable.plugin.webhook.impl;

import com.atlassian.plugin.remotable.plugin.webhook.WebHookRegistration;
import com.atlassian.plugin.remotable.spi.webhook.EventBuilder;
import com.atlassian.plugin.remotable.spi.webhook.MapperBuilder;

public class EventBuilderImpl implements EventBuilder
{
    private final WebHookRegistration registration;

    public EventBuilderImpl(WebHookRegistration registration)
    {
        this.registration = registration;
    }

    @Override
    public <E> MapperBuilder<E> whenFired(Class<E> eventClass)
    {
        registration.setEventTrigger(eventClass);
        return new MapperBuilderImpl(registration);
    }
}
