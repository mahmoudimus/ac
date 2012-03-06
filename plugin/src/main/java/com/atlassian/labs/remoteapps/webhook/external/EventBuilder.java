package com.atlassian.labs.remoteapps.webhook.external;

public interface EventBuilder
{
    MapperBuilder whenFired(Class<?> eventClass);
}
