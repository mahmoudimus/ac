package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.enableFailed")
public class ConnectAddonEnableFailedEvent extends ConnectAddonLifecycleFailedEvent
{
    public ConnectAddonEnableFailedEvent(String addonKey, String message)
    {
        super(addonKey, message, Category.CONNECT); // we never fail to enable because of the add-on
    }
}
