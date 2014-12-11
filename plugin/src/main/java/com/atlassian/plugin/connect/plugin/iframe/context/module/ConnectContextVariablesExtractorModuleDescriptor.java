package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public final class ConnectContextVariablesExtractorModuleDescriptor extends AbstractModuleDescriptor<ContextParametersExtractor>
{
    public ConnectContextVariablesExtractorModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public ContextParametersExtractor getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
