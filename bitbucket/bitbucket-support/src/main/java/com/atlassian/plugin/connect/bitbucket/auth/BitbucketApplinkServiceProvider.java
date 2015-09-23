package com.atlassian.plugin.connect.bitbucket.auth;

import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.plugin.connect.spi.auth.applinks.MutatingApplicationLinkServiceProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

import javax.inject.Inject;

@BitbucketComponent
public class BitbucketApplinkServiceProvider implements MutatingApplicationLinkServiceProvider
{
    private final MutatingApplicationLinkService applicationLinkService;

    @Inject
    public BitbucketApplinkServiceProvider(final MutatingApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    @Override
    public MutatingApplicationLinkService getMutatingApplicationLinkService()
    {
        return applicationLinkService;
    }
}
