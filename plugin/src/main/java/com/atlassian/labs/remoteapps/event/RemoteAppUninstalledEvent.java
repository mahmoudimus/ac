package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppUninstalledEvent extends RemoteAppEvent
{
    public RemoteAppUninstalledEvent(String pluginKey)
    {
        super(pluginKey);
    }
}
