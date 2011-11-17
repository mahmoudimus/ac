package com.atlassian.labs.remoteapps.descriptor.external;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.ModuleDescriptor;
import org.osgi.framework.BundleContext;

public interface AccessLevel
{
    String getId();
    boolean canAccessRemoteApp(String username, ApplicationLink link);

    ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext);
}
