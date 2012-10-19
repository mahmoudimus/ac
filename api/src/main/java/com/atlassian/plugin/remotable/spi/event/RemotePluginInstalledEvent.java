package com.atlassian.plugin.remotable.spi.event;

import java.util.Map;

/**
 * Event that marks the successful installation of a remote plugin
 */
public class RemotePluginInstalledEvent
{
    private final Map<String, Object> data;
    private final String pluginKey;

    public RemotePluginInstalledEvent(String pluginKey, Map<String, Object> build)
    {
        this.data = build;
        this.pluginKey = pluginKey;
    }

    public Map<String, Object> toMap()
    {
        return data;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }
}
