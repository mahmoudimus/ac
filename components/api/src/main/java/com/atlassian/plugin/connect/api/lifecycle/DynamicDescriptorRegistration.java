package com.atlassian.plugin.connect.api.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;

import java.util.Collection;

public interface DynamicDescriptorRegistration {
    Registration registerDescriptors(ModuleDescriptor<?>... descriptors);

    Registration registerDescriptors(Iterable<ModuleDescriptor<?>> descriptors);

    interface Registration {
        void unregister();

        Collection<ModuleDescriptor<?>> getRegisteredDescriptors();
    }
}
