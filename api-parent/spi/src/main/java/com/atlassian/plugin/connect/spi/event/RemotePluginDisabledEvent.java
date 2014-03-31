package com.atlassian.plugin.connect.spi.event;

import java.util.Map;

// analytics?
public final class RemotePluginDisabledEvent extends RemotePluginEvent
{
    public RemotePluginDisabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
