package com.atlassian.labs.remoteapps.webhook.external;

import com.atlassian.labs.remoteapps.webhook.EventSerializationException;

/**
 *
 */
public interface EventSerializer
{
    Object getEvent();
    String getJson() throws EventSerializationException;
}
