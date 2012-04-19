package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.external.ClosableRemoteModule;
import com.atlassian.labs.remoteapps.modules.external.StartableRemoteModule;
import com.atlassian.plugin.ModuleDescriptor;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * Module type for applink application-types
 */
public class ApplicationTypeModule implements ClosableRemoteModule, StartableRemoteModule
{

    private static final Logger log = LoggerFactory.getLogger(ApplicationTypeModule.class);
    private final RemoteAppApplicationType applicationType;
    private final PermissionManager permissionManager;
    private final Set<ModuleDescriptor> descriptors;
    private final MutatingApplicationLinkService applicationLinkService;

    public ApplicationTypeModule(RemoteAppApplicationType applicationType,
            ModuleDescriptor<ApplicationType> applicationTypeDescriptor,
            MutatingApplicationLinkService mutatingApplicationLinkService,
            PermissionManager permissionManager)
    {
        this.applicationType = applicationType;
        this.permissionManager = permissionManager;
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
        final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(applicationType.getDefaultDetails()
                                                                                          .getRpcUrl());
        if (link == null)
        {
            log.info("Creating an application link for the remote app type " + applicationType.getId());

            try
            {
                link = applicationLinkService.getApplicationLink(expectedApplicationId);
                if (link == null)
                {
                    link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, applicationType.getDefaultDetails());
                }
            }
            catch (TypeNotInstalledException e)
            {
                throw new RuntimeException("Type should always be installed", e);
            }
        }
        else
        {
            if (!expectedApplicationId.equals(link.getId()))
            {
                log.debug("Unexpected application id, removing and adding link");
                applicationLinkService.deleteApplicationLink(link);
                applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, applicationType.getDefaultDetails());
            }
            else
            {
                log.info("Applink of type {} already exists", applicationType.getId());
            }
        }
        link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());

        // ensure no permissions by default
        permissionManager.setApiPermissions(applicationType, Collections.<String>emptyList());
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
