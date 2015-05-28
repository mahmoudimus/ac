package com.atlassian.plugin.connect.core.applinks;

import java.net.URI;
import java.util.Set;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestProducer;

import com.google.common.collect.ImmutableSet;

import org.osgi.framework.Version;

/**
 * Manifest for a remote plugin
 */
public class RemotePluginContainerManifestProducer implements ManifestProducer
{
    public Manifest getManifest(final URI url) throws ManifestNotFoundException
    {
        return new Manifest()
        {
            public ApplicationId getId()
            {
                return ApplicationIdUtil.generate(url);
            }

            public String getName()
            {
                return "Plugin Container";
            }

            public TypeId getTypeId()
            {
                return RemotePluginContainerApplicationTypeImpl.TYPE_ID;
            }

            public String getVersion()
            {
                return null;
            }

            public Long getBuildNumber()
            {
                return 1L;
            }

            public URI getUrl()
            {
                return URI.create("https://localhost");
            }

            @Override
            public URI getIconUrl()
            {
                return null;
            }

            public Version getAppLinksVersion()
            {
                return null;
            }

            @Override
            public Set<Class<? extends AuthenticationProvider>> getInboundAuthenticationTypes()
            {
                return ImmutableSet.<Class<? extends AuthenticationProvider>>of(OAuthAuthenticationProvider.class);
            }

            @Override
            public Set<Class<? extends AuthenticationProvider>> getOutboundAuthenticationTypes()
            {
                return ImmutableSet.<Class<? extends AuthenticationProvider>>of(OAuthAuthenticationProvider.class);
            }

            @Override
            public Boolean hasPublicSignup()
            {
                return false;
            }
        };
    }

    public ApplicationStatus getStatus(URI url)
    {
        return ApplicationStatus.AVAILABLE;
    }
}
