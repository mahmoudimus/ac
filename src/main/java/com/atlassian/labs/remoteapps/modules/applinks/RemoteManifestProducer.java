package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestProducer;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.Version;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

/**
 */
public class RemoteManifestProducer implements ManifestProducer
{
    private final TypeId typeId;
    private final String name;

    public RemoteManifestProducer(TypeId typeId, String name)
    {
        this.typeId = typeId;
        this.name = name;
    }

    public Manifest getManifest(final URI url) throws ManifestNotFoundException
    {
        // We don't generate stable IDs, because that would allow us to only have one twitter
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        return new Manifest()
        {
            public ApplicationId getId()
            {
                return ApplicationIdUtil.generate(url);
            }

            public String getName()
            {
                return name;
            }

            public TypeId getTypeId()
            {
                return typeId;
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
