package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public final class ConnectContextVariablesValidatorModuleDescriptor extends AbstractModuleDescriptor<ContextParametersValidator<?>>
{
    public ConnectContextVariablesValidatorModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public ContextParametersValidator<?> getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
