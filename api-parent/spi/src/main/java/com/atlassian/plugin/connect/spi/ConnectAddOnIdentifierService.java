package com.atlassian.plugin.connect.spi;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import org.dom4j.Document;
import org.osgi.framework.Bundle;

import java.io.File;

/**
 * A publicly accessible service to identify if a given plugin is a connect add on
 */
public interface ConnectAddOnIdentifierService
{
    public static final String REMOTE_PLUGIN = "Remote-Plugin";
    
    public static final String CONNECT_ADDON_HEADER = "Atlassian-Connect-Addon";
    
    boolean isConnectAddOn(Bundle bundle);

    boolean isConnectAddOn(Plugin plugin);

    boolean isConnectAddOn(String pluginKey);

    @XmlDescriptor
    boolean isConnectAddOn(Document pluginDescriptor);

    boolean isConnectAddOn(File descriptorFile);

    String getInstallerUser(Bundle bundle);

    String getRegistrationUrl(Bundle bundle);
    
}
