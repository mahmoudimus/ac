package com.atlassian.plugin.connect.spi.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * @since 1.0
 */
public interface ConnectModuleDescriptorFactory<B, D extends ModuleDescriptor>
{

    D createModuleDescriptor(B bean, ConnectAddonBean addon, Plugin plugin);
}
