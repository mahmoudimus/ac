package com.atlassian.plugin.connect.spi.event.product;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

/**
 * Event when the server's build number has been changed
 */
@EventName("connect.plugin.serverUpgraded")
@PrivacyPolicySafe
public final class ServerUpgradedEvent extends UpgradedEvent
{
    public ServerUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }
}
