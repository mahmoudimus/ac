package com.atlassian.plugin.connect.spi.event;

import java.util.Map;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

@PrivacyPolicySafe
@Deprecated
public abstract class RemotePluginEvent
{
    @PrivacyPolicySafe
    private final String pluginKey;

    @PrivacyPolicySafe(false)
    private final Map<String, Object> data;

    protected RemotePluginEvent(String pluginKey, Map<String, Object> data)
    {
        this.pluginKey = checkNotNull(pluginKey);
        this.data = ImmutableMap.copyOf(checkNotNull(data));
    }

    public final Map<String, Object> toMap()
    {
        return ImmutableMap.<String, Object>builder()
                           .put("key", pluginKey)
                           .putAll(data).build();
    }

    public final String getPluginKey()
    {
        return pluginKey;
    }
}
