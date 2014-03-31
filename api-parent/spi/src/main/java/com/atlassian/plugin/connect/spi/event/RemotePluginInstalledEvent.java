package com.atlassian.plugin.connect.spi.event;

import java.util.Map;

/**
 * Event that marks the successful installation of a remote plugin
 */
// analytics?
public final class RemotePluginInstalledEvent extends RemotePluginEvent
{
    public RemotePluginInstalledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
