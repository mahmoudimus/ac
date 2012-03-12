package com.atlassian.labs.remoteapps.webhook.external;

public interface MapperBuilder<E>
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);

    MapperBuilder<E> matchedBy(EventMatcher eventTypeMatcher);
}
