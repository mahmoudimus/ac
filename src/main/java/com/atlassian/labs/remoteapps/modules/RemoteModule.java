package com.atlassian.labs.remoteapps.modules;

import com.atlassian.plugin.ModuleDescriptor;

import java.util.Set;

/**
 *
 */
public interface RemoteModule
{
    Set<ModuleDescriptor> getModuleDescriptors();
}
