package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.plugin.connect.spi.module.ContextVariablesValidator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public final class ConnectContextVariablesValidatorModuleDescriptor extends AbstractModuleDescriptor<ContextVariablesValidator<?>>
{
    public ConnectContextVariablesValidatorModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public ContextVariablesValidator<?> getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
