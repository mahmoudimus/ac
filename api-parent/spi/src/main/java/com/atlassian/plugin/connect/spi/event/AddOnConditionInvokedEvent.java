package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when a Connect add-on RemoteCondition is successfully invoked
 */
@EventName ("connect.addon.remotecondition.invoked")
public class AddOnConditionInvokedEvent extends AddOnConditionEvent
{

    public AddOnConditionInvokedEvent(String pluginKey, String urlPath, long elapsedMillisecs)
    {
        super(pluginKey, urlPath, elapsedMillisecs);
    }

}
