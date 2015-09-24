package com.atlassian.plugin.connect;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class TestJiraModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public TestJiraModuleDescriptor()
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}