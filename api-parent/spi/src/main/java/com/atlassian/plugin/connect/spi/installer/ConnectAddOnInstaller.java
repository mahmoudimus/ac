package com.atlassian.plugin.connect.spi.installer;

import com.atlassian.plugin.Plugin;

/**
 * @since 1.0
 */
public interface ConnectAddOnInstaller
{
    Plugin install(String jsonDescriptor) throws ConnectAddOnInstallException;
}
