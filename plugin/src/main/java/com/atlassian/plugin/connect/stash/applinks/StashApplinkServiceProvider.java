package com.atlassian.plugin.connect.stash.applinks;

import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.plugin.connect.spi.applinks.MutatingApplicationLinkServiceProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;

import javax.inject.Inject;

@StashComponent
public class StashApplinkServiceProvider implements MutatingApplicationLinkServiceProvider
{
    private final MutatingApplicationLinkService applicationLinkService;

    @Inject
    public StashApplinkServiceProvider(final MutatingApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    @Override
    public MutatingApplicationLinkService getMutatingApplicationLinkService()
    {
        return applicationLinkService;
    }
}
