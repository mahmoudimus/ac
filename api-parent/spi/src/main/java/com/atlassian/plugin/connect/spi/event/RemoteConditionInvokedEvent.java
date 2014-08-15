package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Fired when a Connect add-on RemoteCondition is successfully invoked
 */
@EventName ("connect.addon.remotecondition.invoked")
@PrivacyPolicySafe
public class RemoteConditionInvokedEvent extends RemoteConditionEvent
{

    public RemoteConditionInvokedEvent(String pluginKey, URI url, long elapsedMillisecs)
    {
        super(elapsedMillisecs, url, pluginKey);
    }

}
