package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import org.osgi.framework.Bundle;

/**
 *
 */
public interface RemoteAppCreationContext
{
    Plugin getPlugin();

    ModuleDescriptorFactory getModuleDescriptorFactory();

    Bundle getBundle();

    NonAppLinksApplicationType getApplicationType();

    AccessLevel getAccessLevel();
}
