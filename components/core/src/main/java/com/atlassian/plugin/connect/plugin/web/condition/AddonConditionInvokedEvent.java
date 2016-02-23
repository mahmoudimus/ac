package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Fired when a Connect add-on RemoteCondition is successfully invoked
 */
@EventName("connect.addon.remotecondition.invoked")
public class AddonConditionInvokedEvent extends AddonConditionEvent {
    public AddonConditionInvokedEvent(String pluginKey, String urlPath, long elapsedMillisecs) {
        super(pluginKey, urlPath, elapsedMillisecs);
    }
}
