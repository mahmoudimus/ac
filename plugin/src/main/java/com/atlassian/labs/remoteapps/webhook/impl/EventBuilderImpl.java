package com.atlassian.labs.remoteapps.webhook.impl;

import com.atlassian.labs.remoteapps.webhook.WebHookRegistration;
import com.atlassian.labs.remoteapps.webhook.external.EventBuilder;
import com.atlassian.labs.remoteapps.webhook.external.MapperBuilder;

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
