package com.atlassian.plugin.connect.spi.event;

import java.util.Map;

// analytics?
public final class RemotePluginEnabledEvent extends RemotePluginEvent
{
    public RemotePluginEnabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
