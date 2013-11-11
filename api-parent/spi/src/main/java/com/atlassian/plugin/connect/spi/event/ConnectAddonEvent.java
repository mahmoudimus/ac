package com.atlassian.plugin.connect.spi.event;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConnectAddonEvent
{
    private final String pluginKey;
    private final String data;

    protected ConnectAddonEvent(String pluginKey, String data)
    {
        this.pluginKey = checkNotNull(pluginKey);
        this.data = checkNotNull(data);
    }

    public final String getPluginKey()
    {
        return pluginKey;
    }

    public final String getData()
    {
        return data;
    }
}
