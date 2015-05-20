package com.atlassian.plugin.connect.api.integration.plugins;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

import java.util.Collection;

public interface DynamicDescriptorRegistration
{
    Registration registerDescriptors(Plugin plugin, DescriptorToRegister... descriptors);

    Registration registerDescriptors(final Plugin plugin, Iterable<DescriptorToRegister> descriptors);

    public static interface Registration
    {
        void unregister();
        Collection<ModuleDescriptor<?>> getRegisteredDescriptors();
    }
}
