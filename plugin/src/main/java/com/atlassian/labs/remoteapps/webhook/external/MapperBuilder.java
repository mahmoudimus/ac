package com.atlassian.labs.remoteapps.webhook.external;

import com.google.common.base.Predicate;

public interface MapperBuilder<E>
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);

    MapperBuilder<E> matchedBy(Predicate<E> eventTypeMatcher);
}
