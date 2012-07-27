package com.atlassian.labs.remoteapps.webhook.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * Fired when the web hook publishing queue is full and the event will be discarded
 */
public class WebHookPublishQueueFullEvent
{
    private final String eventIdentifier;
    private final String appKey;

    public WebHookPublishQueueFullEvent(String eventIdentifier, String appKey)
    {
        this.eventIdentifier = eventIdentifier;
        this.appKey = appKey;
    }

    public String getEventIdentifier()
    {
        return eventIdentifier;
    }

    public String getAppKey()
    {
        return appKey;
    }
}
