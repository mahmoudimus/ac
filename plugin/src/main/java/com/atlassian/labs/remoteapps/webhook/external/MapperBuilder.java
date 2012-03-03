package com.atlassian.labs.remoteapps.webhook.external;

import com.atlassian.labs.remoteapps.product.jira.JiraWebHookProvider;
import com.google.common.base.Predicate;

public interface MapperBuilder<E>
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);

    MapperBuilder<E> matchedBy(Predicate<E> eventTypeMatcher);
}
