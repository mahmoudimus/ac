package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
abstract public class RemoteConditionEvent
{
    @PrivacyPolicySafe
    protected final String pluginKey;
    // TODO: check that nothing private is in these urls
    @PrivacyPolicySafe
    protected final URI url;
    @PrivacyPolicySafe
    protected final long elapsedMillisecs;

    public RemoteConditionEvent(long elapsedMillisecs, URI url, String pluginKey)
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
