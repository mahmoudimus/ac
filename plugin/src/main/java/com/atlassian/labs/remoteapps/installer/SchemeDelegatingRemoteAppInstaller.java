package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
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
    private final FileRemoteAppInstaller fileInstaller;

    @Autowired
    public SchemeDelegatingRemoteAppInstaller(DefaultRemoteAppInstaller httpInstaller,
            FileRemoteAppInstaller fileInstaller)
    {
        this.httpInstaller = httpInstaller;
        this.fileInstaller = fileInstaller;
    }

    @Override
    public String install(String username, URI registrationUrl, String registrationSecret,
            boolean stripUnknownModules, KeyValidator keyValidator) throws
            PermissionDeniedException
    {
        if ("file".equals(registrationUrl.getScheme()))
        {
            return fileInstaller.install(username, registrationUrl, registrationSecret,
                    stripUnknownModules, keyValidator);
        }
        else
        {
            return httpInstaller.install(username, registrationUrl, registrationSecret,
                    stripUnknownModules, keyValidator);
        }
    }
}
