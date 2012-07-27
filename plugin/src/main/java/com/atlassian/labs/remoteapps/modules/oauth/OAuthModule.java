package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.modules.external.StartableRemoteModule;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.ModuleDescriptor;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Establishes a bi-directional oauth link
 */
public class OAuthModule implements StartableRemoteModule
{
    private final OAuthLinkManager oAuthLinkManager;
    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final Consumer consumer;
    private final ServiceProvider serviceProvider;
    private final String appKey;

    public OAuthModule(OAuthLinkManager oAuthLinkManager, ApplicationLinkAccessor applicationLinkAccessor,
                       Consumer consumer, ServiceProvider serviceProvider, String appKey)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.consumer = consumer;
        this.serviceProvider = serviceProvider;
        this.appKey = appKey;
    }

    @Override
    public Set<ModuleDescriptor> getModuleDescriptors()
    {
        return emptySet();
    }

    @Override
    public void start()
    {
        ApplicationLink link = applicationLinkAccessor.getApplicationLink(appKey);

        oAuthLinkManager.associateConsumerWithLink(link, consumer);

        oAuthLinkManager.associateProviderWithLink(link, consumer.getKey(),
                                                   serviceProvider);
    }
}
