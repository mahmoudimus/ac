package com.atlassian.plugin.connect.spi.event.product;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Event when the Remotable Plugins plugin itself version has changed
 */
@EventName("connect.plugin.upgraded")
public final class PluginsUpgradedEvent extends UpgradedEvent
{
    public PluginsUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }
}
