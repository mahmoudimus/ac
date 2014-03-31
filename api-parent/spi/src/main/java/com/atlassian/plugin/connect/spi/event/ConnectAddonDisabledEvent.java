package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when the remote application's mirror plugin is disabled
 */
@EventName ("connect.addon.disabled")
@PrivacyPolicySafe
public class ConnectAddonDisabledEvent extends ConnectAddonLifecycleWithDataEvent
{
    public ConnectAddonDisabledEvent(String pluginKey, String data)
    {
        super(pluginKey, data);
    }
}
