package com.atlassian.plugin.connect.spi.event;

import java.util.Map;

public final class RemotePluginDisabledEvent extends RemotePluginEvent
{
    public RemotePluginDisabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
