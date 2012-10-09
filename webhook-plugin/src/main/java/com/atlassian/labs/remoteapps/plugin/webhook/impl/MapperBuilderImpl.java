package com.atlassian.labs.remoteapps.plugin.webhook.impl;

import com.atlassian.labs.remoteapps.plugin.webhook.WebHookRegistration;
import com.atlassian.labs.remoteapps.spi.webhook.EventMatcher;
import com.atlassian.labs.remoteapps.spi.webhook.EventSerializerFactory;
import com.atlassian.labs.remoteapps.spi.webhook.MapperBuilder;

public class MapperBuilderImpl<E> implements MapperBuilder<E>
{
    private final WebHookRegistration registration;

    public MapperBuilderImpl(WebHookRegistration registration)
    {
        this.registration = registration;
    }

    @Override
    public void serializedWith(EventSerializerFactory eventSerializerFactory)
    {
        registration.setEventSerializerFactory(eventSerializerFactory);
    }

    @Override
    public MapperBuilder<E> matchedBy(EventMatcher eventTypeMatcher)
    {
        registration.setEventMatcher(eventTypeMatcher);
        return this;
    }
}
