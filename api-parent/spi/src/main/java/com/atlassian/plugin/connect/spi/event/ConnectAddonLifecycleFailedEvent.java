package com.atlassian.plugin.connect.spi.event;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
public abstract class ConnectAddonLifecycleFailedEvent extends ConnectAddonLifecycleEvent
{
    private static final int MAX_MESSAGE_LENGTH = 100;

    /**
     * The HTTP status code of the failed HTTP lifecycle request
     */
    @PrivacyPolicySafe
    private final Integer statusCode;

    /**
     * The reason why the lifecycle event failed
     */
    @PrivacyPolicySafe
    private final String message;

    public ConnectAddonLifecycleFailedEvent(String pluginKey)
    {
        this(pluginKey, null);
    }

    public ConnectAddonLifecycleFailedEvent(String pluginKey, String message)
    {
        this(pluginKey, null, message);
    }

    public ConnectAddonLifecycleFailedEvent(String pluginKey, Integer statusCode, String message)
    {
        super(pluginKey);
        this.statusCode = statusCode;
        this.message = StringUtils.substring(message, 0, MAX_MESSAGE_LENGTH);
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public String getMessage()
    {
        return message;
    }
}
