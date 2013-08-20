package com.atlassian.plugin.connect.plugin.installer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.MalformedURLException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.descriptor.util.FormatConverter;
import com.atlassian.plugin.connect.plugin.descriptor.util.XmlUtils;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.atlassian.upm.spi.PluginInstallResult;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * @since version
 */
public class ConnectUPMInstallHandler implements PluginInstallHandler
{
    private final ConnectAddOnIdentifierService connectIdentifier;
    private final ConnectAddOnInstaller connectInstaller;
    private final UserManager userManager;
    private final FormatConverter formatConverter;

    public ConnectUPMInstallHandler(ConnectAddOnIdentifierService connectIdentifier, ConnectAddOnInstaller connectInstaller, UserManager userManager, FormatConverter formatConverter)
    {
        this.connectIdentifier = connectIdentifier;
        this.connectInstaller = connectInstaller;
        this.userManager = userManager;
        this.formatConverter = formatConverter;
    }

    @Override
    public boolean canInstallPlugin(File descriptorFile, Option<String> contentType)
    {
        boolean caninstall = connectIdentifier.isConnectAddOn(descriptorFile);
        
        return caninstall;
    }

    @Override
    public PluginInstallResult installPlugin(File descriptorFile, Option<String> contentType) throws PluginInstallException
    {
        try
        {
            //TODO: get rid of formatConverter when we go to capabilities
            Document doc = formatConverter.readFileToDoc(descriptorFile);
            
            String username = "unknown";

            UserProfile up = userManager.getRemoteUser();
            
            if(null != up)
            {
                username = up.getUsername();    
            }
            
            Plugin plugin = connectInstaller.install(username,doc);
            
            return new PluginInstallResult(plugin);
        }
        catch(Exception e)
        {
            throw new PluginInstallException("unable to install connect add on", e);
        }
    }
    
}
