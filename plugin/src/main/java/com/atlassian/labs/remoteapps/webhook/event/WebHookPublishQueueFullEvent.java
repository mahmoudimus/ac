package com.atlassian.labs.remoteapps.webhook.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * Fired when the web hook publishing queue is full and the event will be discarded
 */
public class WebHookPublishQueueFullEvent
{
    private final String eventIdentifier;
    private final ApplicationLink applicationLink;

    public WebHookPublishQueueFullEvent(String eventIdentifier, ApplicationLink applicationLink)
    {
        this.eventIdentifier = eventIdentifier;
        this.applicationLink = applicationLink;
    }

    public String getEventIdentifier()
    {
        return eventIdentifier;
    }

    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }
}
