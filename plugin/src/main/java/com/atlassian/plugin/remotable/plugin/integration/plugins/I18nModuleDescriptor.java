package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 *
 */
public final class I18nModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public I18nModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public Void getModule()
    {
        throw new UnsupportedOperationException();
    }
}
