package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

import java.util.Map;

@EventName ("connect.legacy.addon.enabled")
@Deprecated
public final class RemotePluginEnabledEvent extends RemotePluginEvent
{
    public RemotePluginEnabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
