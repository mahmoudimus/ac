package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
abstract public class RemoteConditionEvent
{
    @PrivacyPolicySafe
    private final String pluginKey;
    // TODO: check that nothing private is in these urls

    @PrivacyPolicySafe
    private final URI url;

    @PrivacyPolicySafe
    private final long elapsedMillisecs;

    public RemoteConditionEvent(String pluginKey, URI url, long elapsedMillisecs)
    {
        this.elapsedMillisecs = elapsedMillisecs;
        this.url = url;
        this.pluginKey = pluginKey;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public URI getUrl()
    {
        return url;
    }

    public long getElapsedMillisecs()
    {
        return elapsedMillisecs;
    }
}
