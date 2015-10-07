package com.atlassian.plugin.connect.spi.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;

/**
 * @since 1.0
 */
public interface ConnectModuleDescriptorFactory<B, D extends ModuleDescriptor>
{
    D createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, B bean);
}
