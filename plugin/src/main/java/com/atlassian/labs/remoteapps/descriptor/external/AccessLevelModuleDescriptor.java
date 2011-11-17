package com.atlassian.labs.remoteapps.descriptor.external;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 *
 */
public class AccessLevelModuleDescriptor extends AbstractModuleDescriptor<AccessLevel>
{
    private final ModuleFactory moduleFactory;

    public AccessLevelModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        this.moduleFactory = moduleFactory;
    }

    @Override
    public AccessLevel getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
