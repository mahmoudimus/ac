package com.atlassian.plugin.connect.plugin.module.provider;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;

public class ConnectModuleListProviderModuleDescriptor extends AbstractModuleDescriptor<Object>
{
    private final ResettableLazyReference<Object> moduleLazyReference = new ResettableLazyReference<Object>()
    {
        @Override
        protected Object create() throws Exception
        {
            return moduleFactory.createModule(moduleClassName, ConnectModuleListProviderModuleDescriptor.this);
        }
    };

    public ConnectModuleListProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public Object getModule()
    {
        return moduleLazyReference.get();
    }

}
