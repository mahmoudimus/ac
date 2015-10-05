package com.atlassian.plugin.connect.spi;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface RemotablePluginAccessorFactory
{
    /**
     * Supplies an accessor for remote plugin operations. Instances are only meant to be used for the current operation
     * and should not be cached across operations.
     *
     * @param pluginKey The plugin key
     * @return An accessor for either local or remote plugin operations
     */
    RemotablePluginAccessor get(String pluginKey);

    /**
     * Supplies an accessor for remote plugin operations. Instances are only meant to be used for the current operation
     * and should not be cached across operations.
     *
     * @param addon The addon bean
     * @return An accessor for either local or remote plugin operations
     */
    RemotablePluginAccessor get(ConnectAddonBean addon);

    RemotablePluginAccessor getOrThrow(String pluginKey) throws IllegalStateException;

    void remove(String pluginKey);
}
