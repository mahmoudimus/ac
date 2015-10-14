package com.atlassian.plugin.connect.api.integration.plugins;

import com.atlassian.plugin.ModuleDescriptor;

/**
 *
 */
public final class DescriptorToRegister
{
    private final ModuleDescriptor descriptor;

    public DescriptorToRegister(ModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public ModuleDescriptor getDescriptor()
    {
        return descriptor;
    }
}
