package com.atlassian.plugin.connect.api.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.beans.CapabilityBean;

/**
 * @since version
 */
public interface ConnectModuleProvider<T>
{
    List<ModuleDescriptor> provideModules(Plugin plugin, List<T> beans);
}
