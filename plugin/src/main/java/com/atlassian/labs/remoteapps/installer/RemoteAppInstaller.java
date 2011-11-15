package com.atlassian.labs.remoteapps.installer;

/**
 * Installs a remote app
 */
public interface RemoteAppInstaller
{
    /**
     * Installs a remote app using a registration URL
     * @param registrationUrl The registration URL to retrieve the Remote App info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     */
    void install(String registrationUrl, String registrationSecret);
}
