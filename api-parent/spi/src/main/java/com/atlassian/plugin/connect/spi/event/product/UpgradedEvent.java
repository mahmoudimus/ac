package com.atlassian.plugin.connect.spi.event.product;

/**
 * Event type for version upgrades
 */
public abstract class UpgradedEvent
{
    protected final String oldVersion;

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
