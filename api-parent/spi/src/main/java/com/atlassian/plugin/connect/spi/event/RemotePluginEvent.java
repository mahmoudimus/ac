package com.atlassian.plugin.connect.spi.event;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Deprecated
public abstract class RemotePluginEvent
{
    private final String pluginKey;

    private final Map<String, Object> data;

    protected RemotePluginEvent(String pluginKey, Map<String, Object> data)
    {
        this.pluginKey = checkNotNull(pluginKey);
        this.data = ImmutableMap.copyOf(checkNotNull(data));
    }

    public final Map<String, Object> toMap()
    {
        return ImmutableMap.<String, Object>builder()
                           .put("key", pluginKey)
                           .putAll(data).build();
    }

    public final String getPluginKey()
    {
        return pluginKey;
    }
}
