package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppEvent
{
    protected final String pluginKey;

    public RemoteAppEvent(String pluginKey)
    {
        this.pluginKey = pluginKey;
    }

    public String getRemoteAppKey()
    {
        return pluginKey;
    }
}
