package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

import java.util.Map;

/**
 * Event that marks the successful installation of a remote plugin
 */
@EventName ("connect.legacy.addon.installed")
@Deprecated
public final class RemotePluginInstalledEvent extends RemotePluginEvent
{
    public RemotePluginInstalledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
