package com.atlassian.labs.remoteapps.spi.webhook;

public interface MapperBuilder<E>
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);

    MapperBuilder<E> matchedBy(EventMatcher eventTypeMatcher);
}
