package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppStartedEvent extends RemoteAppEvent
{
    public RemoteAppStartedEvent(String appKey)
    {
        super(appKey);
    }
}
