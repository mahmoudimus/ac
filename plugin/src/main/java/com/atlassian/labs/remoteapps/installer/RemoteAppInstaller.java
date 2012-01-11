package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.PermissionDeniedException;

/**
 * Installs a remote app
 */
public interface RemoteAppInstaller
{
    public static interface KeyValidator
    {
        void validate(String appKey) throws PermissionDeniedException;
    }
    /**
     * Installs a remote app using a registration URL
     * @param username The user
     * @param registrationUrl The registration URL to retrieve the Remote App info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     */
    String install(String username, String registrationUrl, String registrationSecret, KeyValidator keyValidator) throws PermissionDeniedException;

    void uninstall(String username) throws PermissionDeniedException;
}
