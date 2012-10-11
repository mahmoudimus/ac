package com.atlassian.plugin.remotable.spi.webhook;

/**
 *
 */
public interface EventSerializer
{
    Object getEvent();
    String getJson() throws EventSerializationException;
}
