package com.atlassian.plugin.connect.api.integration.plugins;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

import java.util.Collection;

public interface DynamicDescriptorRegistration
{
    Registration registerDescriptors(ModuleDescriptor<?>... descriptors);

    Registration registerDescriptors(Iterable<ModuleDescriptor<?>> descriptors);

    public static interface Registration
    {
        void unregister();
        Collection<ModuleDescriptor<?>> getRegisteredDescriptors();
    }
}
