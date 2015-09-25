package com.atlassian.plugin.connect;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class JiraModuleDescriptorForTests extends AbstractModuleDescriptor<Void>
{
    public JiraModuleDescriptorForTests()
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}