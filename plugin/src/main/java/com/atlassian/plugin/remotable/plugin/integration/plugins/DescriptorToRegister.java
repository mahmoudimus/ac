package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.ModuleDescriptor;

import java.util.Properties;

/**
 *
 */
public final class DescriptorToRegister
{
    private final ModuleDescriptor descriptor;

    private final Properties i18nProperties;

    public DescriptorToRegister(ModuleDescriptor descriptor)
    {
        this(descriptor, null);
    }
    public DescriptorToRegister(ModuleDescriptor descriptor, Properties i18nProperties)
    {
        this.descriptor = descriptor;
        this.i18nProperties = i18nProperties;
    }


    public ModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    public Properties getI18nProperties()
    {
        return i18nProperties;
    }
}
