package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when the remote application's mirror plugin is enabled
 */
@EventName ("connect.addon.enabled")
@PrivacyPolicySafe
public class ConnectAddonEnabledEvent extends ConnectAddonLifecycleWithDataEvent
{
    public ConnectAddonEnabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
