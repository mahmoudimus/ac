package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when the remote application's mirror plugin is enabled
 */
@EventName ("connect.addon.enabled") // todo exclude data
public class ConnectAddonEnabledEvent extends ConnectAddonEvent
{
    public ConnectAddonEnabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
