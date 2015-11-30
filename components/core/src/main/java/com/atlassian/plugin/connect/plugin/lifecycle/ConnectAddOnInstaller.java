package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.ConnectAddonInstallException;

/**
 * @since 1.0
 */
public interface ConnectAddOnInstaller
{
    Plugin install(String jsonDescriptor) throws ConnectAddonInstallException;
}
