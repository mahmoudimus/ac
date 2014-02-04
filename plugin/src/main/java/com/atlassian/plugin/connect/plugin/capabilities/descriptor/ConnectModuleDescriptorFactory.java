package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;

/**
 * @since 1.0
 */
public interface ConnectModuleDescriptorFactory<B, D extends ModuleDescriptor>
{
    D createModuleDescriptor(Plugin plugin, B bean);
}
