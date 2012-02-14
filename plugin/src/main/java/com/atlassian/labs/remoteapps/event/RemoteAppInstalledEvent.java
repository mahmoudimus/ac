package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppInstalledEvent extends RemoteAppEvent
{

    public RemoteAppInstalledEvent(String pluginKey)
    {
        super(pluginKey);
    }

}
