package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

import java.util.Map;

@EventName ("connect.legacy.addon.disabled")
@PrivacyPolicySafe
@Deprecated
public final class RemotePluginDisabledEvent extends RemotePluginEvent
{
    public RemotePluginDisabledEvent(String pluginKey, Map<String, Object> data)
    {
        super(pluginKey, data);
    }
}
