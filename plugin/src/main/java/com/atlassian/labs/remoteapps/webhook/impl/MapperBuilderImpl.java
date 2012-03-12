package com.atlassian.labs.remoteapps.webhook.impl;

import com.atlassian.labs.remoteapps.webhook.WebHookRegistration;
import com.atlassian.labs.remoteapps.webhook.external.EventMatcher;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializerFactory;
import com.atlassian.labs.remoteapps.webhook.external.MapperBuilder;
import com.google.common.base.Predicate;

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
