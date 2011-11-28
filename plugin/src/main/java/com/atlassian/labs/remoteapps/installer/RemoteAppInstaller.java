package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.PermissionDeniedException;

/**
 * Installs a remote app
 */
public interface RemoteAppInstaller
{
    /**
     * Installs a remote app using a registration URL
     * @param username The user
     * @param registrationUrl The registration URL to retrieve the Remote App info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     */
    void install(String username, String registrationUrl, String registrationSecret) throws PermissionDeniedException;
}
