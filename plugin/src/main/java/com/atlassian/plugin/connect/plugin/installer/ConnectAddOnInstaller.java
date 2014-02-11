package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;

import com.atlassian.upm.spi.PluginInstallException;
import org.dom4j.Document;

/**
 * @since 1.0
 */
public interface ConnectAddOnInstaller
{
    Plugin install(String username, Document document) throws PluginInstallException;

    Plugin install(String username, String capabilities) throws PluginInstallException;
}
