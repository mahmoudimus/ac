package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when a failure has occured invoking a Connect add-on RemoteCondition
 */
@EventName ("connect.addon.remotecondition.failed")
@PrivacyPolicySafe
public class RemoteConditionFailedEvent extends RemoteConditionEvent
{
    private static final int MAX_MESSAGE_LENGTH = 100;
    @PrivacyPolicySafe
    private final String message;

    public RemoteConditionFailedEvent(String pluginKey, String urlPath, long elapsedMillisecs, String message)
    {
        super(pluginKey, urlPath, elapsedMillisecs);
        this.message = StringUtils.substring(message, 0, MAX_MESSAGE_LENGTH);
    }

    public String getMessage()
    {
        return message;
    }
}
