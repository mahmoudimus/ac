package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@EventName ("connect.addon.enableFailed")
@PrivacyPolicySafe
public class ConnectAddonEnableFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonEnableFailedEvent(String pluginKey, String message)
    {
        super(pluginKey, message);
    }
}
