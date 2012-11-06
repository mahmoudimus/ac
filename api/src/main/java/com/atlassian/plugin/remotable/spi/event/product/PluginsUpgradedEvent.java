package com.atlassian.plugin.remotable.spi.event.product;

/**
 * Event when the Remotable Plugins plugin itself version has changed
 */
public final class PluginsUpgradedEvent extends UpgradedEvent
{
    public PluginsUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }
}
