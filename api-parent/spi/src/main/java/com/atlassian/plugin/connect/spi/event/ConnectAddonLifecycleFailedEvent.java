package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

public abstract class ConnectAddonLifecycleFailedEvent extends ConnectAddonLifecycleEvent
{
    /**
     * The HTTP status code of the failed HTTP lifecycle request
     */
    @PrivacyPolicySafe
    private final int statusCode;

    /**
     * The status text of the failed HTTP lifecycle request
     */
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
