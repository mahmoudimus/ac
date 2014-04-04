package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@EventName ("connect.legacy.addon.installFailed")
@PrivacyPolicySafe
@Deprecated
public class RemotePluginInstallFailedEvent
{
    /**
     * The HTTP status code of the failed HTTP lifecycle request
     */
    @PrivacyPolicySafe
    private final String pluginKey;
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

    public RemotePluginInstallFailedEvent(String pluginKey, int statusCode, String statusText)
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
