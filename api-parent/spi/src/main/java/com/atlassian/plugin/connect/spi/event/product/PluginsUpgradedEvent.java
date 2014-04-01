package com.atlassian.plugin.connect.spi.event.product;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Event when the Remotable Plugins plugin itself version has changed
 */
@EventName("connect.plugin.upgraded")
@PrivacyPolicySafe
public final class PluginsUpgradedEvent extends UpgradedEvent
{
    public PluginsUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }
}
