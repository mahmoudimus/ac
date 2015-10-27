package com.atlassian.plugin.connect.plugin.lifecycle.analytics;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when a Connect add-on is installed
 */
@EventName ("connect.addon.installed")
public class ConnectAddonInstalledEvent extends ConnectAddonLifecycleEvent
{
    public  ConnectAddonInstalledEvent(String pluginKey)
    {
        super(pluginKey);
    }
}
