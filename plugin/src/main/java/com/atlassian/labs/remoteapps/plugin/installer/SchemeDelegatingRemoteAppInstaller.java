package com.atlassian.labs.remoteapps.plugin.installer;

import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Determines the correct installer impl from the uri scheme
 */
@Component
public class SchemeDelegatingRemoteAppInstaller implements RemoteAppInstaller
{
    private final DefaultRemoteAppInstaller httpInstaller;

    @Autowired
    public SchemeDelegatingRemoteAppInstaller(DefaultRemoteAppInstaller httpInstaller)
    {
        this.httpInstaller = httpInstaller;
    }

    @Override
    public String install(String username, URI registrationUrl, String registrationSecret,
            boolean stripUnknownModules, KeyValidator keyValidator) throws
            PermissionDeniedException
    {
        return httpInstaller.install(username, registrationUrl, registrationSecret,
                stripUnknownModules, keyValidator);
    }
}
