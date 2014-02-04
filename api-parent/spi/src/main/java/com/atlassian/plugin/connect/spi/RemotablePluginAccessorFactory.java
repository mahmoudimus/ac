package com.atlassian.plugin.connect.spi;

public interface RemotablePluginAccessorFactory
{
    RemotablePluginAccessor get(String pluginKey);

    RemotablePluginAccessor getOrThrow(String pluginKey) throws IllegalStateException;
}
