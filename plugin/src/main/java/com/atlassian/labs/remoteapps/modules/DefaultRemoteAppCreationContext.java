package com.atlassian.labs.remoteapps.modules;

import com.atlassian.labs.remoteapps.RemoteAppAccessor;
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
    private final RemoteAppAccessor remoteAppAccessor;

    public DefaultRemoteAppCreationContext(Plugin plugin,
            ModuleDescriptorFactory moduleDescriptorFactory,
            Bundle bundle,
            RemoteAppAccessor remoteAppAccessor)
    {
        this.plugin = plugin;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.bundle = bundle;
        this.remoteAppAccessor = remoteAppAccessor;
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
    public RemoteAppAccessor getRemoteAppAccessor()
    {
        return remoteAppAccessor;
    }

    @Override
    public ModuleDescriptorFactory getModuleDescriptorFactory()
    {
        return moduleDescriptorFactory;
    }
}
