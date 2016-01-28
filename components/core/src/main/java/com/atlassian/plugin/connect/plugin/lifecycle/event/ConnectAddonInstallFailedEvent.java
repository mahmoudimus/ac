package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.installFailed")
public class ConnectAddonInstallFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonInstallFailedEvent(String pluginKey, String message, Category category)
    {
        super(pluginKey, message, category);
    }

    public ConnectAddonInstallFailedEvent(String pluginKey, int statusCode, String statusText, Category category)
    {
        super(pluginKey, statusCode, statusText, category);
    }
}
