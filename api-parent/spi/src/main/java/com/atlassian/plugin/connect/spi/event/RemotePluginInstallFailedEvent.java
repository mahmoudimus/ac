package com.atlassian.plugin.connect.spi.event;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@EventName ("connect.legacy.addon.installFailed")
@PrivacyPolicySafe
@Deprecated
public class RemotePluginInstallFailedEvent
{
    private static final int MAX_MESSAGE_LENGTH = 100;
    /**
     * The HTTP status code of the failed HTTP lifecycle request
     */
    @PrivacyPolicySafe
    private final String pluginKey;
    /**
     * The HTTP status code of the failed HTTP lifecycle request
     */
    @PrivacyPolicySafe
    private final Integer statusCode;

    /**
     * The status text of the failed HTTP lifecycle request
     */
    @PrivacyPolicySafe
    private final String statusText;

    public RemotePluginInstallFailedEvent(String pluginKey, String message)
    {
        this(pluginKey, null, message);
    }

    public RemotePluginInstallFailedEvent(String pluginKey, Integer statusCode, String statusText)
    {
        this.pluginKey = pluginKey;
        this.statusCode = statusCode;
        this.statusText = StringUtils.substring(statusText, 0, MAX_MESSAGE_LENGTH );
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }
}
