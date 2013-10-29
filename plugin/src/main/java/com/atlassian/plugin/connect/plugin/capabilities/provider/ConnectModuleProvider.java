package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

import org.osgi.framework.BundleContext;

/**
 * @since 1.0
 */
public interface ConnectModuleProvider<T>
{
    List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<T> beans);
}
