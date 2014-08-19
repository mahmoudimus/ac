package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
abstract public class RemoteConditionEvent
{
    @PrivacyPolicySafe
    private final String addonKey;

    @PrivacyPolicySafe
    private final String urlPath;

    @PrivacyPolicySafe
    private final long elapsedMillisecs;

    public RemoteConditionEvent(String addonKey, String urlPath, long elapsedMillisecs)
    {
        this.elapsedMillisecs = elapsedMillisecs;
        this.urlPath = urlPath;
        this.addonKey = addonKey;
    }

    public String getAddonKey()
    {
        return addonKey;
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
