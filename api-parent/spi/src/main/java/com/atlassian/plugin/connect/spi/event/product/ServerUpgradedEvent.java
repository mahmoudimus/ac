package com.atlassian.plugin.connect.spi.event.product;

/**
 * Event when the server's build number has been changed
 */
public final class ServerUpgradedEvent extends UpgradedEvent
{
    public ServerUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }

}
