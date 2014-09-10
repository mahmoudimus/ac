package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

import java.util.Map;

@EventName ("connect.legacy.addon.disabled")
@Deprecated
public final class RemotePluginDisabledEvent extends RemotePluginEvent
{
    public RemotePluginDisabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
