package com.atlassian.plugin.connect.testsupport;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import java.io.IOException;

public interface TestPluginInstaller
{
    Plugin installAddon(ConnectAddonBean bean) throws IOException;

    Plugin installAddon(String jsonDescriptor) throws IOException;

    void uninstallAddon(Plugin plugin) throws IOException;

    void disableAddon(String pluginKey) throws IOException;

    void enableAddon(String pluginKey) throws IOException;

    String getInternalAddonBaseUrl(String pluginKey);
    String getInternalAddonBaseUrlSuffix(String pluginKey, String additionalSuffix); // no product base url in return value
    
    Iterable<String> getInstalledAddonKeys();
}
