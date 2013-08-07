package com.atlassian.plugin.connect.plugin.installer;

import java.net.URI;

import com.atlassian.plugin.connect.spi.PermissionDeniedException;

import org.dom4j.Document;

/**
 * Installs a remote plugin
 */
public interface RemotePluginInstaller
{
    /**
     * Installs a remote plugin using a registration URL
     *
     * @param username            The user
     * @param registrationUrl     The registration URL to retrieve the Remotable Plugin info
     * @param descriptor          The plugin descriptor
     */
    String install(String username, URI registrationUrl, Document descriptor) throws
            PermissionDeniedException;
}
