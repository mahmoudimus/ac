package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

public abstract class ConnectAddonLifecycleFailedEvent extends ConnectAddonLifecycleEvent
{
    @PrivacyPolicySafe
    private final int statusCode;

    @PrivacyPolicySafe
    private final String statusText;

    public ConnectAddonLifecycleFailedEvent(String pluginKey, int statusCode, String statusText)
    {
        super(pluginKey);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }
}
