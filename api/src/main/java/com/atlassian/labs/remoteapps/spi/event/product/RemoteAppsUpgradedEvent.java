package com.atlassian.labs.remoteapps.spi.event.product;

/**
 * Event when the Remote Apps plugin itself version has changed
 */
public class RemoteAppsUpgradedEvent extends UpgradedEvent
{

    public RemoteAppsUpgradedEvent(String oldVersion, String newVersion)
    {
        super(oldVersion, newVersion);
    }

}
