package com.atlassian.labs.remoteapps.descriptor.factory;

import com.atlassian.labs.remoteapps.installer.AccessLevel;
import com.atlassian.plugin.ModuleDescriptor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AggregateDescriptorFactory
{
    private final GlobalDescriptorFactory globalDescriptorFactory;
    private final UserAccessLevelDescriptorFactory userAccessLevelDescriptorFactory;

    @Autowired
    public AggregateDescriptorFactory(GlobalDescriptorFactory globalDescriptorFactory,
                                      UserAccessLevelDescriptorFactory userAccessLevelDescriptorFactory)
    {
        this.globalDescriptorFactory = globalDescriptorFactory;
        this.userAccessLevelDescriptorFactory = userAccessLevelDescriptorFactory;
    }


    public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext, AccessLevel accessLevel)
    {
        switch (accessLevel)
        {
            case GLOBAL : return globalDescriptorFactory.createWebItemModuleDescriptor();
            case PER_USER: return userAccessLevelDescriptorFactory.createWebItemModuleDescriptor(targetBundleContext);
            default : throw new IllegalArgumentException();
        }
    }
}
