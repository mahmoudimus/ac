package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInstallException;

/**
 * @since 1.0
 */
public interface ConnectAddonInstaller
{
    Plugin install(String jsonDescriptor) throws ConnectAddonInstallException;
}
