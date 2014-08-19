package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when a failure has occured invoking a Connect add-on RemoteCondition
 */
@EventName ("connect.addon.remotecondition.failed")
@PrivacyPolicySafe
public class RemoteConditionFailedEvent extends RemoteConditionEvent
{
    @PrivacyPolicySafe
    private final String message;

    public RemoteConditionFailedEvent(String pluginKey, String urlPath, long elapsedMillisecs, String message)
    {
        super(pluginKey, urlPath, elapsedMillisecs);
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }
}
