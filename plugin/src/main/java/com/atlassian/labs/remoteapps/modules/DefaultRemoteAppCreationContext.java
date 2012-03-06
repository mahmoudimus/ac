package com.atlassian.labs.remoteapps.modules;

import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import org.osgi.framework.Bundle;

/**
 * Context for remote app creation and initialization
 */
public class DefaultRemoteAppCreationContext implements RemoteAppCreationContext
{
    private final Plugin plugin;
    private final ModuleDescriptorFactory moduleDescriptorFactory;
    private final Bundle bundle;
    private final RemoteAppApplicationType applicationType;

    public DefaultRemoteAppCreationContext(Plugin plugin,
                                           ModuleDescriptorFactory moduleDescriptorFactory,
                                           Bundle bundle,
                                           RemoteAppApplicationType applicationType
    )
    {
        this.plugin = plugin;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.bundle = bundle;
        this.applicationType = applicationType;
    }

    @Override
    public Plugin getPlugin()
    {
        return plugin;
    }

    @Override
    public Bundle getBundle()
    {
        return bundle;
    }

    @Override
    public RemoteAppApplicationType getApplicationType()
    {
        return applicationType;
    }

    @Override
    public ModuleDescriptorFactory getModuleDescriptorFactory()
    {
        return moduleDescriptorFactory;
    }
}
