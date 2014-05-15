package com.atlassian.plugin.connect.testsupport;

import java.io.File;
import java.io.IOException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface TestPluginInstaller
{

    Plugin installAddon(ConnectAddonBean bean) throws IOException;

    Plugin installAddon(String jsonDescriptor) throws IOException;

    Plugin installPlugin(File jarFile) throws IOException;

    void uninstallAddon(Plugin plugin) throws IOException;

    void uninstallPlugin(Plugin plugin) throws IOException;

    void disableAddon(String pluginKey) throws IOException;

    void enableAddon(String pluginKey) throws IOException;
    
    void disablePlugin(String pluginKey) throws IOException;

    void enablePlugin(String pluginKey) throws IOException;

    String getInternalAddonBaseUrl(String pluginKey);
    
    Iterable<String> getInstalledAddonKeys();
}
