package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.labs.remoteapps.modules.ClosableRemoteModule;
import com.atlassian.labs.remoteapps.modules.StartableRemoteModule;
import com.atlassian.plugin.ModuleDescriptor;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 */
public class ApplicationTypeModule implements ClosableRemoteModule, StartableRemoteModule
{

    private static final Logger log = LoggerFactory.getLogger(ApplicationTypeModule.class);
    private final RemoteAppApplicationType applicationType;
    private final Set<ModuleDescriptor> descriptors;
    private final MutatingApplicationLinkService applicationLinkService;

    public ApplicationTypeModule(RemoteAppApplicationType applicationType, ModuleDescriptor<ApplicationType> applicationTypeDescriptor, MutatingApplicationLinkService mutatingApplicationLinkService)
    {
        this.applicationType = applicationType;
        this.descriptors = ImmutableSet.<ModuleDescriptor>of(applicationTypeDescriptor);
        this.applicationLinkService = mutatingApplicationLinkService;
    }


    @Override
    public Set<ModuleDescriptor> getModuleDescriptors()
    {
        return descriptors;
    }

    @Override
    public void start()
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
        if (link == null)
        {
            log.info("Creating an application link for the remote app type " + applicationType.getId());
            final ApplicationId applicationId = ApplicationIdUtil.generate(applicationType.getDefaultDetails().getRpcUrl());
            try
            {
                if (applicationLinkService.getApplicationLink(applicationId) == null)
                {
                    applicationLinkService.addApplicationLink(applicationId, applicationType, applicationType.getDefaultDetails());
                }
            }
            catch (TypeNotInstalledException e)
            {
                throw new RuntimeException("Type should always be installed", e);
            }
        }
        else
        {
            log.info("Applink of type {} already exists", applicationType.getId());
        }
    }

    @Override
    public void close()
    {
    }

    public RemoteAppApplicationType getApplicationType()
    {
        return applicationType;
    }
}
