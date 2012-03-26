package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.api.PermissionDeniedException;

/**
 * Installs a remote app
 */
public interface RemoteAppInstaller
{
    public static interface KeyValidator
    {
        void validatePermissions(String appKey) throws PermissionDeniedException;
    }
    /**
     * Installs a remote app using a registration URL
     * @param username The user
     * @param registrationUrl The registration URL to retrieve the Remote App info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     * @param stripUnknownModules Whether unknown modules should be stripped
     */
    String install(String username, String registrationUrl, String registrationSecret,
            boolean stripUnknownModules, KeyValidator keyValidator) throws
                                                                                                                  PermissionDeniedException;
}
