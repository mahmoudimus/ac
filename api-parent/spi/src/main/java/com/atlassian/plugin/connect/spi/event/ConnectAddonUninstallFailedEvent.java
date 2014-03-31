package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@EventName ("connect.addon.uninstallFailed")
@PrivacyPolicySafe
public class ConnectAddonUninstallFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonUninstallFailedEvent(final String pluginKey, int statusCode, String statusText)
    {
        super(pluginKey, statusCode, statusText);
    }
}
