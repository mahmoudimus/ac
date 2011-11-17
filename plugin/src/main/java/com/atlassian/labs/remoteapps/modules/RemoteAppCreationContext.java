package com.atlassian.labs.remoteapps.modules;

import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import org.osgi.framework.Bundle;

/**
 * Context for remote app creation and initialization
 */
public class RemoteAppCreationContext
{
    private final Plugin plugin;
    private final ModuleDescriptorFactory moduleDescriptorFactory;
    private final Bundle bundle;
    private final NonAppLinksApplicationType applicationType;
    private final AccessLevel accessLevel;

    public RemoteAppCreationContext(Plugin plugin, ModuleDescriptorFactory moduleDescriptorFactory, Bundle bundle, NonAppLinksApplicationType applicationType,
                                    AccessLevel accessLevel)
    {
        this.plugin = plugin;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.bundle = bundle;
        this.applicationType = applicationType;
        this.accessLevel = accessLevel;
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

    public AccessLevel getAccessLevel()
    {
        return accessLevel;
    }
}
