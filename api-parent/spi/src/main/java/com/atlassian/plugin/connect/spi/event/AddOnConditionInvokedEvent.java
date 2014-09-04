package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when a Connect add-on RemoteCondition is successfully invoked
 */
@EventName ("connect.addon.remotecondition.invoked")
@PrivacyPolicySafe
public class AddOnConditionInvokedEvent extends AddOnConditionEvent
{

    public AddOnConditionInvokedEvent(String pluginKey, String urlPath, long elapsedMillisecs)
    {
        super(pluginKey, urlPath, elapsedMillisecs);
    }

}
