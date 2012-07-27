package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.labs.remoteapps.RemoteAppAccessor;
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

    RemoteAppAccessor getRemoteAppAccessor();

}
