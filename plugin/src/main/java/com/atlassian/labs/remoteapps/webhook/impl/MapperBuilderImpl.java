package com.atlassian.labs.remoteapps.webhook.impl;

import com.atlassian.labs.remoteapps.webhook.WebHookRegistration;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializerFactory;
import com.atlassian.labs.remoteapps.webhook.external.MapperBuilder;

public class MapperBuilderImpl implements MapperBuilder
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
}
