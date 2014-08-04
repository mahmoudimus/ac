package com.atlassian.plugin.connect.spi.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.plugin.capabilities.provider.ConnectModuleProviderContext;

/**
 * @since 1.0
 */
public interface ConnectModuleDescriptorFactory<B, D extends ModuleDescriptor>
{
    D createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, B bean);
}
