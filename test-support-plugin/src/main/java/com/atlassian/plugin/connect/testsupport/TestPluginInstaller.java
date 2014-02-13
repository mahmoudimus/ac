package com.atlassian.plugin.connect.testsupport;

import java.io.File;
import java.io.IOException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface TestPluginInstaller
{

    Plugin installPlugin(ConnectAddonBean bean) throws IOException;
    
    Plugin installPlugin(File jarFile) throws IOException;

    void uninstallPlugin(Plugin plugin) throws IOException;

    void disablePlugin(String pluginKey) throws IOException;

    void enablePlugin(String pluginKey) throws IOException;

    String getInternalAddonBaseUrl(String pluginKey);
}
