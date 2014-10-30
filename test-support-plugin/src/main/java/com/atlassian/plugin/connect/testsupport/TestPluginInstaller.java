package com.atlassian.plugin.connect.testsupport;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import java.io.File;
import java.io.IOException;

public interface TestPluginInstaller
{
    Plugin installAddon(ConnectAddonBean bean) throws IOException;

    Plugin installAddon(String jsonDescriptor) throws IOException;

    @XmlDescriptor
    @Deprecated
    Plugin installPlugin(File jarFile) throws IOException;

    void uninstallJsonAddon(Plugin plugin) throws IOException;

    @XmlDescriptor
    @Deprecated
    void uninstallXmlAddon(Plugin plugin) throws IOException;

    void disableAddon(String pluginKey) throws IOException;

    void enableAddon(String pluginKey) throws IOException;

    @XmlDescriptor
    @Deprecated
    void disablePlugin(String pluginKey) throws IOException;

    @XmlDescriptor
    @Deprecated
    void enablePlugin(String pluginKey) throws IOException;

    String getInternalAddonBaseUrl(String pluginKey);
    String getInternalAddonBaseUrlSuffix(String pluginKey, String additionalSuffix); // no product base url in return value
    
    Iterable<String> getInstalledAddonKeys();
}
