package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.spi.PermissionDeniedException;

import java.net.URI;

/**
 * Installs a remote plugin
 */
public interface RemotePluginInstaller
{
    public static interface KeyValidator
    {
        void validatePermissions(String appKey) throws PermissionDeniedException;
    }
    /**
     * Installs a remote plugin using a registration URL
     * @param username The user
     * @param registrationUrl The registration URL to retrieve the Remotable Plugin info
     * @param registrationSecret The secret token to send to the registration URL.  Can be null.
     * @param stripUnknownModules Whether unknown modules should be stripped
     */
    String install(String username, URI registrationUrl, String registrationSecret,
            boolean stripUnknownModules, KeyValidator keyValidator) throws
                                                                                                                  PermissionDeniedException;
}
