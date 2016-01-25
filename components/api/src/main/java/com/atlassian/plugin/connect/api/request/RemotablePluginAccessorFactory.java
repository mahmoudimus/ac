package com.atlassian.plugin.connect.api.request;

public interface RemotablePluginAccessorFactory
{
    /**
     * Supplies an accessor for remote plugin operations. Instances are only meant to be used for the current operation
     * and should not be cached across operations.
     *
     * @param pluginKey The plugin key
     * @return An accessor for either local or remote plugin operations
     * @throws IllegalStateException if no appropriate {@link RemotablePluginAccessor} can be created. This normally means
     *  that data has been restored from an instance with a different base url, breaking the Applink. See: AC-1528
     */
    RemotablePluginAccessor get(String pluginKey) throws IllegalStateException;

    void remove(String pluginKey);
}
