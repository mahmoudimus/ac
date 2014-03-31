package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

@EventName ("connect.addon.installFailed")
public class ConnectAddonInstallFailedEvent
{
    private final String pluginKey;
    private final int statusCode;
    private final String statusText;

    public ConnectAddonInstallFailedEvent(String pluginKey, int statusCode, String statusText)
    {
        this.pluginKey = pluginKey;
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public String getPluginKey()
    {
        return pluginKey;
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
