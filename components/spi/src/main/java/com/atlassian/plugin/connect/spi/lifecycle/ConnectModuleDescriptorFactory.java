package com.atlassian.plugin.connect.spi.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

/**
 * @since 1.0
 */
public interface ConnectModuleDescriptorFactory<B, D extends ModuleDescriptor>
{
    D createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, B bean);
}
