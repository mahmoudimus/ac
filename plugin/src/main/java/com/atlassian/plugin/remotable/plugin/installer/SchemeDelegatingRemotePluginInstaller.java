package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Determines the correct installer impl from the uri scheme
 */
@Component
public class SchemeDelegatingRemotePluginInstaller implements RemotePluginInstaller
{
    private final DefaultRemotePluginInstaller httpInstaller;

    @Autowired
    public SchemeDelegatingRemotePluginInstaller(DefaultRemotePluginInstaller httpInstaller)
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
