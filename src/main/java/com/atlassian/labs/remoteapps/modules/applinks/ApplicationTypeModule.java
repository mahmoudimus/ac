package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.plugin.ModuleDescriptor;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 *
 */
public class ApplicationTypeModule implements RemoteModule
{


    private final RemoteAppApplicationType applicationType;
    private final Set<ModuleDescriptor> descriptors;
    private ApplinkCreator applinkCreator;

    public ApplicationTypeModule(RemoteAppApplicationType applicationType, ModuleDescriptor<ApplicationType> applicationTypeDescriptor, ApplinkCreator applinkCreator)
    {
        this.applicationType = applicationType;
        this.descriptors = ImmutableSet.<ModuleDescriptor>of(applicationTypeDescriptor);
        this.applinkCreator = applinkCreator;
    }

    @Override
    public Set<ModuleDescriptor> getModuleDescriptors()
    {
        return descriptors;
    }

    @Override
    public void close()
    {
        applinkCreator.destroy();
    }

    public RemoteAppApplicationType getApplicationType()
    {
        return applicationType;
    }
}
