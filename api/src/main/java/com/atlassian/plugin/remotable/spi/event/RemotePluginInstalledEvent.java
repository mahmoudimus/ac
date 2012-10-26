package com.atlassian.plugin.remotable.spi.event;

import java.util.Map;

/**
 * Event that marks the successful installation of a remote plugin
 */
public final class RemotePluginInstalledEvent extends RemotePluginEvent
{
    public RemotePluginInstalledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
