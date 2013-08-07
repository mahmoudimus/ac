package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;

public interface DescribedModuleDescriptorFactoryAccessor
{
    Iterable<DescribedModuleDescriptorFactory> getDescribedModuleDescriptorFactories();
}
