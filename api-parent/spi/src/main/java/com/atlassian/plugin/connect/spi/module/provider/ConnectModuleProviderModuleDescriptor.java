package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;

public class ConnectModuleProviderModuleDescriptor extends AbstractModuleDescriptor<Object>
{
    private final ResettableLazyReference<Object> moduleLazyReference = new ResettableLazyReference<Object>()
    {
        @Override
        protected Object create() throws Exception
        {
            System.out.println("HI WE'RE CREATING THE LAZY REFERENCE");
            return moduleFactory.createModule(moduleClassName, ConnectModuleProviderModuleDescriptor.this);
        }
    };

    public ConnectModuleProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        System.out.println("HI WE'RE CONSTRUCTING THE CONNECTMODULEPROVIDERMODULEDESCRIPTOR");
    }

    @Override
    public Object getModule()
    {
        System.out.println("HI WE'RE GETTING THE MODULE");
        return moduleLazyReference.get();
    }
}
