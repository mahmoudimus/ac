package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.plugin.ModuleDescriptor;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * A remote module that provides descriptors
 */
public interface RemoteModule
{
    public RemoteModule NO_OP = new RemoteModule()
    {
        @Override
        public Set<ModuleDescriptor> getModuleDescriptors()
        {
            return emptySet();
        }
    };
    Set<ModuleDescriptor> getModuleDescriptors();
}
