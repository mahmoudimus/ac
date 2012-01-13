package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.modules.external.StartableRemoteModule;
import com.atlassian.labs.remoteapps.modules.external.UninstallableRemoteModule;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.ModuleDescriptor;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 12/01/12
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class OAuthModule implements StartableRemoteModule, UninstallableRemoteModule
{
    private final OAuthLinkManager oAuthLinkManager;
    private final ApplicationLinkService applicationLinkService;
    private final Consumer consumer;
    private final ServiceProvider serviceProvider;
    private final NonAppLinksApplicationType applicationType;

    public OAuthModule(OAuthLinkManager oAuthLinkManager, ApplicationLinkService applicationLinkService,
                       Consumer consumer, ServiceProvider serviceProvider, NonAppLinksApplicationType applicationType)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.applicationLinkService = applicationLinkService;
        this.consumer = consumer;
        this.serviceProvider = serviceProvider;
        this.applicationType = applicationType;
    }

    @Override
    public Set<ModuleDescriptor> getModuleDescriptors()
    {
        return emptySet();
    }

    @Override
    public void start()
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());

        oAuthLinkManager.associateConsumerWithLink(link, consumer);

        oAuthLinkManager.associateProviderWithLink(link, consumer.getKey(),
                                                   serviceProvider);
    }

    @Override
    public void uninstall()
    {
        oAuthLinkManager.unassociateConsumer(consumer);
    }
}
