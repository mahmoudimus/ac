package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when the remote application's mirror plugin is disabled
 */
@EventName ("connect.addon.disabled")
public class ConnectAddonDisabledEvent extends ConnectAddonEvent
{
    public ConnectAddonDisabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
