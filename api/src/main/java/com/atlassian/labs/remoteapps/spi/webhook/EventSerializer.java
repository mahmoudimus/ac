package com.atlassian.labs.remoteapps.spi.webhook;

/**
 *
 */
public interface EventSerializer
{
    Object getEvent();
    String getJson() throws EventSerializationException;
}
