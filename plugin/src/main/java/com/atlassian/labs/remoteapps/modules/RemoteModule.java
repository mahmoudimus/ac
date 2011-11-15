package com.atlassian.labs.remoteapps.modules;

import com.atlassian.plugin.ModuleDescriptor;

import java.util.Set;

/**
 * A remote module that provides descriptors
 */
public interface RemoteModule
{
    Set<ModuleDescriptor> getModuleDescriptors();
}
