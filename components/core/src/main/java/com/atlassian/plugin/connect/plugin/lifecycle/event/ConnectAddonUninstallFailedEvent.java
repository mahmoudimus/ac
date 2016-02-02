package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.uninstallFailed")
public class ConnectAddonUninstallFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonUninstallFailedEvent(String pluginKey, String message)
    {
        super(pluginKey, message, Category.CONNECT); // we never fail to uninstall because of the add-on
    }
}
