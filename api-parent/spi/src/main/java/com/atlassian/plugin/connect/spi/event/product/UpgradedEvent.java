package com.atlassian.plugin.connect.spi.event.product;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Event type for version upgrades
 */
@PrivacyPolicySafe
public abstract class UpgradedEvent
{
    @PrivacyPolicySafe
    protected final String oldVersion;

    @PrivacyPolicySafe
    protected final String newVersion;

    public UpgradedEvent(String oldVersion, String newVersion)
    {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public String getOldVersion()
    {
        return oldVersion;
    }

    public String getNewVersion()
    {
        return newVersion;
    }
}
