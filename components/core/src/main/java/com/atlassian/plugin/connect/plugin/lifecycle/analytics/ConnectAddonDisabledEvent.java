package com.atlassian.plugin.connect.plugin.lifecycle.analytics;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when the remote application's mirror plugin is disabled
 */
@EventName ("connect.addon.disabled")
public class ConnectAddonDisabledEvent extends ConnectAddonLifecycleWithDataEvent
{
    public ConnectAddonDisabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
