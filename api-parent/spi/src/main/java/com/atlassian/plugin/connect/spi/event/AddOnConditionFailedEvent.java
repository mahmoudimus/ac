package com.atlassian.plugin.connect.spi.event;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when a failure has occured invoking a Connect add-on RemoteCondition
 */
@EventName ("connect.addon.remotecondition.failed")
public class AddOnConditionFailedEvent extends AddOnConditionEvent
{
    private static final int MAX_MESSAGE_LENGTH = 100;
    private final String message;

    public AddOnConditionFailedEvent(String pluginKey, String urlPath, long elapsedMillisecs, String message)
    {
        super(pluginKey, urlPath, elapsedMillisecs);
        this.message = StringUtils.substring(message, 0, MAX_MESSAGE_LENGTH);
    }

    public String getMessage()
    {
        return message;
    }
}
