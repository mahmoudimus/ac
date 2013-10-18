package com.atlassian.plugin.connect.plugin.installer;

import java.io.File;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.descriptor.util.FormatConverter;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.atlassian.upm.spi.PluginInstallResult;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import org.dom4j.Document;
import org.osgi.framework.BundleContext;

/**
 * @since 1.0
 */
public class ConnectUPMInstallHandler implements PluginInstallHandler
{
    private final ConnectAddOnIdentifierService connectIdentifier;
    private final ConnectAddOnInstaller connectInstaller;
    private final UserManager userManager;
    private final FormatConverter formatConverter;
    private final BundleContext bundleContext;

    public ConnectUPMInstallHandler(ConnectAddOnIdentifierService connectIdentifier, ConnectAddOnInstaller connectInstaller, UserManager userManager, FormatConverter formatConverter, BundleContext bundleContext)
    {
        this.connectIdentifier = connectIdentifier;
        this.connectInstaller = connectInstaller;
        this.userManager = userManager;
        this.formatConverter = formatConverter;
        this.bundleContext = bundleContext;
    }

    @Override
    public boolean canInstallPlugin(File descriptorFile, Option<String> contentType)
    {
        boolean caninstall = connectIdentifier.isConnectAddOn(descriptorFile);
        
        if(!caninstall)
        {
            try
            {
                String json = Files.toString(descriptorFile,Charsets.UTF_8);
                ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson(bundleContext).fromJson(json, ConnectAddonBean.class);
                
                caninstall = (null != addOn && !Strings.isNullOrEmpty(addOn.getKey()));
            }
            catch (Exception e)
            {
                caninstall = false;
            }
        }
        
        return caninstall;
    }

    @Override
    public PluginInstallResult installPlugin(File descriptorFile, Option<String> contentType) throws PluginInstallException
    {
        try
        {
            boolean isXml = connectIdentifier.isConnectAddOn(descriptorFile);
            Plugin plugin;
            
            if(isXml)
            {
                //TODO: get rid of formatConverter when we go to capabilities
                Document doc = formatConverter.readFileToDoc(descriptorFile);
                
                plugin = connectInstaller.install(userManager.getRemoteUsername(),doc);
            }
            else
            {
                String json = Files.toString(descriptorFile,Charsets.UTF_8);
                plugin = connectInstaller.install(userManager.getRemoteUsername(),json);
            }
            
            return new PluginInstallResult(plugin);
        }
        catch(Exception e)
        {
            throw new PluginInstallException("unable to install connect add on", e);
        }
    }
    
}
