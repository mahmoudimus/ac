package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProvider;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.atlassian.util.concurrent.Supplier;

public class ConnectModuleProviderModuleDescriptor extends AbstractModuleDescriptor<ConnectModuleProvider>
{
    private final Supplier<ConnectModuleProvider> moduleLazyReference = new ResettableLazyReference<ConnectModuleProvider>()
    {
        @Override
        protected ConnectModuleProvider create() throws Exception
        {
            return moduleFactory.createModule(moduleClassName, ConnectModuleProviderModuleDescriptor.this);
        }
    };

    public ConnectModuleProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }
    
    @Override
    public ConnectModuleProvider getModule()
    {
        return moduleLazyReference.get();
    }
}
