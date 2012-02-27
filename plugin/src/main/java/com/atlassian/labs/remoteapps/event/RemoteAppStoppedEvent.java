package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppStoppedEvent extends RemoteAppEvent
{
    public RemoteAppStoppedEvent(String appKey)
    {
        super(appKey);
    }
}
