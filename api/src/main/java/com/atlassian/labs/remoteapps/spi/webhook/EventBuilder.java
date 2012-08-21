package com.atlassian.labs.remoteapps.spi.webhook;

public interface EventBuilder
{
    <E> MapperBuilder<E> whenFired(Class<E> eventClass);
}
