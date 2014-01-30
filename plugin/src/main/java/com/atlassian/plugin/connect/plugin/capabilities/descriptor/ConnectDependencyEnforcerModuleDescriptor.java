package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class ConnectDependencyEnforcerModuleDescriptor extends AbstractModuleDescriptor<Void>
{

    public ConnectDependencyEnforcerModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
