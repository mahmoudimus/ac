package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when a Connect add-on is installed
 */
@EventName ("connect.addon.installed")
@PrivacyPolicySafe
public class ConnectAddonInstalledEvent extends ConnectAddonLifecycleEvent
{
    public ConnectAddonInstalledEvent(String pluginKey)
    {
        super(pluginKey);
    }
}
