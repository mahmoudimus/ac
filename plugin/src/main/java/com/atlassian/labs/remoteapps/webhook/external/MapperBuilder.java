package com.atlassian.labs.remoteapps.webhook.external;

public interface MapperBuilder
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);
}
