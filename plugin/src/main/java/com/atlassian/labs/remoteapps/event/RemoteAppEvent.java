package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppEvent
{
    protected final String remoteAppKey;

    public RemoteAppEvent(String remoteAppKey)
    {
        this.remoteAppKey = remoteAppKey;
    }

    public String getRemoteAppKey()
    {
        return remoteAppKey;
    }
}
