package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;

/**
 * @since 1.0
 */
public interface ConnectModuleDescriptorFactory<B, D extends ModuleDescriptor>
{
    D createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, B bean);
}
