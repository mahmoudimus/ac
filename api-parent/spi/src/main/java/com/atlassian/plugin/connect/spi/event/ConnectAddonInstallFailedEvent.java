package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.installFailed")
public class ConnectAddonInstallFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonInstallFailedEvent(String pluginKey, String message)
    {
        super(pluginKey, message);
    }

    public ConnectAddonInstallFailedEvent(String pluginKey, int statusCode, String statusText)
    {
        super(pluginKey, statusCode, statusText);
    }
}
