package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * A module type for api scopes, usually api-scope
 */
public class ApiScopeModuleDescriptor extends AbstractModuleDescriptor<ApiScope>
{
    private final ModuleFactory moduleFactory;

    public ApiScopeModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        this.moduleFactory = moduleFactory;
    }

    @Override
    public ApiScope getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
