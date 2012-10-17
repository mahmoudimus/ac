package com.atlassian.plugin.remotable.spi;

import java.util.Set;

/**
 * Main remotable plugins functions
 */
public interface RemotablePluginInstallationService
{
    /**
     * Installs a remote plugin using a registration URL
     *
     * @param username            The user
     * @param registrationUrl     The registration URL to retrieve the Remotable Plugin info
     * @param registrationSecret  The secret token to send to the registration URL.  Can be null.
     */
    String install(String username, String registrationUrl, String registrationSecret) throws
            PermissionDeniedException, InstallationFailedException;

    void uninstall(String username, String appKey) throws PermissionDeniedException;

    String getPluginKey(String registrationUrl);

    Set<String> reinstallRemotePlugins(String remoteUsername);
}
