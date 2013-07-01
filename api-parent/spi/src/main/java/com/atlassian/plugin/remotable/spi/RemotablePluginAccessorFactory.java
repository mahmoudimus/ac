package com.atlassian.plugin.remotable.spi;

public interface RemotablePluginAccessorFactory
{
    RemotablePluginAccessor get(String pluginKey);
}
