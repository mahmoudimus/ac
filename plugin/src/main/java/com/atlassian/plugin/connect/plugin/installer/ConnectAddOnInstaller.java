package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.upm.spi.PluginInstallException;

/**
 * @since 1.0
 */
public interface ConnectAddOnInstaller
{
    Plugin install(String jsonDescriptor) throws PluginInstallException;
}
