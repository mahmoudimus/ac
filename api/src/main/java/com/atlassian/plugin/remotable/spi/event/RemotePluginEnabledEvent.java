package com.atlassian.plugin.remotable.spi.event;

import java.util.Map;

public final class RemotePluginEnabledEvent extends RemotePluginEvent
{
    public RemotePluginEnabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
