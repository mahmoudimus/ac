package com.atlassian.plugin.connect.spi;

import com.atlassian.plugin.Plugin;

public interface RemotablePluginAccessorFactory
{
    RemotablePluginAccessor get(String pluginKey);

    RemotablePluginAccessor get(Plugin plugin);

    RemotablePluginAccessor getOrThrow(String pluginKey) throws IllegalStateException;

    void remove(String pluginKey);
}
