package com.atlassian.plugin.remotable.spi.webhook;

public interface EventBuilder
{
    <E> MapperBuilder<E> whenFired(Class<E> eventClass);
}
