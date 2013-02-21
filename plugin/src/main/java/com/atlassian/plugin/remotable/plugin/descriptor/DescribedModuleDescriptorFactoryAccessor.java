package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;

public interface DescribedModuleDescriptorFactoryAccessor
{
    Iterable<DescribedModuleDescriptorFactory> getDescribedModuleDescriptorFactories();
}
