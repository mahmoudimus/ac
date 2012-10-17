package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.util.concurrent.Promise;

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
     *
     * @param username            The user
     * @param registrationUrl     The registration URL to retrieve the Remotable Plugin info
     * @param registrationSecret  The secret token to send to the registration URL.  Can be null.
     */
    String install(String username, URI registrationUrl, String registrationSecret,
            KeyValidator keyValidator) throws
            PermissionDeniedException;

    Promise<String> getPluginKey(URI registrationUrl);
}
