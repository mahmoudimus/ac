package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@EventName ("connect.addon.uninstallFailed")
@PrivacyPolicySafe
public class ConnectAddonUninstallFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonUninstallFailedEvent(String pluginKey, String message)
    {
        super(pluginKey, message);
    }

    public ConnectAddonUninstallFailedEvent(String pluginKey, int statusCode, String message)
    {
        super(pluginKey, statusCode, message);
    }
}
