package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * This is a marker class for configure pages.
 * It's here so we have a registration for configure pages.
 * If this didn't exist, then addons that only have a configure page would never be enabled
 */
public class ConfigurePageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public ConfigurePageModuleDescriptor()
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
