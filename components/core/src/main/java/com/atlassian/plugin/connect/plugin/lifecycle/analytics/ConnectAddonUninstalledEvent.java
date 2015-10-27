package com.atlassian.plugin.connect.plugin.lifecycle.analytics;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when the remote application's mirror plugin is uninstalled
 */
@EventName ("connect.addon.uninstalled")
public class ConnectAddonUninstalledEvent extends ConnectAddonLifecycleEvent
{
    public ConnectAddonUninstalledEvent(String pluginKey)
    {
        super(pluginKey);
    }
}