package com.atlassian.plugin.connect.plugin.web.page;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class PostInstallPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public PostInstallPageModuleDescriptor()
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
