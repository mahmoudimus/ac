package com.atlassian.plugin.remotable.spi.event.product;

/**
 * Event when the server's build number has been changed
 */
public class ServerUpgradedEvent extends UpgradedEvent
{
    public ServerUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }

}
