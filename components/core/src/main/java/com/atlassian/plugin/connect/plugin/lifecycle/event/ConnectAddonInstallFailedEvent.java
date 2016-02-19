package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.installFailed")
public class ConnectAddonInstallFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonInstallFailedEvent(String addonKey, String message, Category category)
    {
        super(addonKey, message, category);
    }

    public ConnectAddonInstallFailedEvent(String addonKey, int statusCode, String statusText, Category category)
    {
        super(addonKey, statusCode, statusText, category);
    }
}
