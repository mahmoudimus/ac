package com.atlassian.plugin.connect.jira.applinks;

import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.plugin.connect.spi.applinks.MutatingApplicationLinkServiceProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import javax.inject.Inject;

@JiraComponent
public class JiraApplinkServiceProvider implements MutatingApplicationLinkServiceProvider
{
    private final MutatingApplicationLinkService applicationLinkService;

    @Inject
    public JiraApplinkServiceProvider(final MutatingApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    @Override
    public MutatingApplicationLinkService getMutatingApplicationLinkService()
    {
        return applicationLinkService;
    }
}
