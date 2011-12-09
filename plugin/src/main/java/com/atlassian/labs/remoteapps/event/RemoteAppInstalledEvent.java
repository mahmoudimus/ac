package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppInstalledEvent
{
    private final String pluginKey;
    private final String accessLevel;

    public RemoteAppInstalledEvent(String pluginKey, String accessLevel)
    {
        this.pluginKey = pluginKey;
        this.accessLevel = accessLevel;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public String getAccessLevel()
    {
        return accessLevel;
    }
}
