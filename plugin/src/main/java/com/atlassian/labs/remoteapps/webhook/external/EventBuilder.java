package com.atlassian.labs.remoteapps.webhook.external;

public interface EventBuilder
{
    <E> MapperBuilder<E> whenFired(Class<E> eventClass);
}
