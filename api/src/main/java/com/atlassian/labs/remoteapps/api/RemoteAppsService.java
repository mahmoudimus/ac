package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;

/**
 * Main remote apps functions
 */
public interface RemoteAppsService
{
    /**
     * Installs a remote app using a registration URL
     *
     * @param username           The user
     * @param registrationUrl    The registration URL to retrieve the Remote App info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     */
    String install(String username, String registrationUrl, String registrationSecret) throws
            PermissionDeniedException, InstallationFailedException;

    void uninstall(String username, String appKey) throws PermissionDeniedException;
}
