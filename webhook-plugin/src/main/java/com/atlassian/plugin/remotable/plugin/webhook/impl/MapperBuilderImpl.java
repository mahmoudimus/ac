package com.atlassian.plugin.remotable.plugin.webhook.impl;

import com.atlassian.plugin.remotable.plugin.webhook.WebHookRegistration;
import com.atlassian.plugin.remotable.spi.webhook.EventMatcher;
import com.atlassian.plugin.remotable.spi.webhook.EventSerializerFactory;
import com.atlassian.plugin.remotable.spi.webhook.MapperBuilder;

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
