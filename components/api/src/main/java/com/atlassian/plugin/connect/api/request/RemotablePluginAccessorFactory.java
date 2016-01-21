package com.atlassian.plugin.connect.api.request;

public interface RemotablePluginAccessorFactory
{
    /**
     * Supplies an accessor for remote plugin operations. Instances are only meant to be used for the current operation
     * and should not be cached across operations.
     *
     * @param pluginKey The plugin key
     * @return An accessor for either local or remote plugin operations
     */
    RemotablePluginAccessor get(String pluginKey) throws IllegalStateException;

    void remove(String pluginKey);
}
