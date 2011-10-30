package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import org.osgi.framework.Bundle;

/**
 *
 */
public class RemoteAppCreationContext
{
    private final Plugin plugin;
    private final ModuleDescriptorFactory moduleDescriptorFactory;
    private final Bundle bundle;
    private final NonAppLinksApplicationType applicationType;

    public RemoteAppCreationContext(Plugin plugin, ModuleDescriptorFactory moduleDescriptorFactory, Bundle bundle, NonAppLinksApplicationType applicationType)
    {
        this.plugin = plugin;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.bundle = bundle;
        this.applicationType = applicationType;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public ModuleDescriptorFactory getModuleDescriptorFactory()
    {
        return moduleDescriptorFactory;
    }

    public Bundle getBundle()
    {
        return bundle;
    }

    public NonAppLinksApplicationType getApplicationType()
    {
        return applicationType;
    }
}
