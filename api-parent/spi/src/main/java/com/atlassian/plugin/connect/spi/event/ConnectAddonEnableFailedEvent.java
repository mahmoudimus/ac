package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.enableFailed")
public class ConnectAddonEnableFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonEnableFailedEvent(String pluginKey, String message)
    {
        super(pluginKey, message);
    }
}
