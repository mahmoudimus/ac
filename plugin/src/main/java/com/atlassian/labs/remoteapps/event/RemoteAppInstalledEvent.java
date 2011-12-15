package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppInstalledEvent extends RemoteAppEvent
{
    private final String accessLevel;

    public RemoteAppInstalledEvent(String pluginKey, String accessLevel)
    {
        super(pluginKey);
        this.accessLevel = accessLevel;
    }

    public String getAccessLevel()
    {
        return accessLevel;
    }

    @Deprecated
    public String getPluginKey()
    {
        return getRemoteAppKey();
    }
}
