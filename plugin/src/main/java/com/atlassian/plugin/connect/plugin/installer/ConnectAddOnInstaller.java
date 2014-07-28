package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.upm.spi.PluginInstallException;
import org.dom4j.Document;

/**
 * @since 1.0
 */
public interface ConnectAddOnInstaller
{
    @Deprecated
    @XmlDescriptor
    Plugin install(String username, Document document) throws PluginInstallException;

    Plugin install(String jsonDescriptor) throws PluginInstallException;
}
