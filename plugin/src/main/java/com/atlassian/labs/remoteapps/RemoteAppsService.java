package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.installer.InstallationFailedException;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 12/01/12
 * Time: 12:02 AM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteAppsService
{
    /**
     * Installs a remote app using a registration URL
     * @param username The user
     * @param registrationUrl The registration URL to retrieve the Remote App info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     */
    String install(String username, String registrationUrl, String registrationSecret) throws PermissionDeniedException,
                                                                                              InstallationFailedException;

    void uninstall(String username, String appKey) throws PermissionDeniedException;
}
