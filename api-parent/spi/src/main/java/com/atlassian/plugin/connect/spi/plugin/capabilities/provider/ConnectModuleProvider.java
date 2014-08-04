package com.atlassian.plugin.connect.spi.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

/**
 * @since 1.0
 */
public interface ConnectModuleProvider<T>
{
    // TODO(ACDEV-1344): pull plugin and jsonFieldName into context
    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin plugin,
                                          String jsonFieldName, List<T> beans);
}
