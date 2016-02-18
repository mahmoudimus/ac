package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when the remote application's mirror plugin is uninstalled
 */
@EventName ("connect.addon.uninstalled")
public class ConnectAddonUninstalledEvent extends ConnectAddonLifecycleEvent
{
    public ConnectAddonUninstalledEvent(String addonKey)
    {
        super(addonKey);
    }
}
