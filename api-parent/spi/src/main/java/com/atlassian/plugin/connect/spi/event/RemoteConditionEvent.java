package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
abstract public class RemoteConditionEvent
{
    @PrivacyPolicySafe
    private final String pluginKey;

    @PrivacyPolicySafe
    private final String urlPath;

    @PrivacyPolicySafe
    private final long elapsedMillisecs;

    public RemoteConditionEvent(String pluginKey, String urlPath, long elapsedMillisecs)
    {
        this.elapsedMillisecs = elapsedMillisecs;
        this.urlPath = urlPath;
        this.pluginKey = pluginKey;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public String getUrlPath()
    {
        return urlPath;
    }

    public long getElapsedMillisecs()
    {
        return elapsedMillisecs;
    }
}
