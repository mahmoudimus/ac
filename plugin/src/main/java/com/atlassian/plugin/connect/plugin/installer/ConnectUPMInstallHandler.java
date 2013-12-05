package com.atlassian.plugin.connect.plugin.installer;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.descriptor.util.FormatConverter;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.atlassian.upm.spi.PluginInstallResult;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.0
 */
@ExportAsService(PluginInstallHandler.class)
@Named
public class ConnectUPMInstallHandler implements PluginInstallHandler
{
    private static final Logger log = LoggerFactory.getLogger(ConnectUPMInstallHandler.class);

    private final LegacyAddOnIdentifierService connectIdentifier;
    private final ConnectAddOnInstaller connectInstaller;
    private final UserManager userManager;
    private final FormatConverter formatConverter;
    private final BundleContext bundleContext;

    @Inject
    public ConnectUPMInstallHandler(LegacyAddOnIdentifierService connectIdentifier, ConnectAddOnInstaller connectInstaller, UserManager userManager, FormatConverter formatConverter, BundleContext bundleContext)
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
        boolean isConnectXml = connectIdentifier.isConnectAddOn(descriptorFile);
        boolean canInstall = isConnectXml;

        if (!isConnectXml)
        {
            try
            {
                String json = Files.toString(descriptorFile, Charsets.UTF_8);
                ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson(bundleContext).fromJson(json, ConnectAddonBean.class);

                canInstall = (null != addOn && !Strings.isNullOrEmpty(addOn.getKey()));
            }
            catch (Exception e)
            {
                log.error(ConnectUPMInstallHandler.class.getSimpleName() + " can not install descriptor " +
                            descriptorFile.getName(), e);
                canInstall = false;
            }
        }

        return canInstall;
    }

    @Override
    public PluginInstallResult installPlugin(File descriptorFile, Option<String> contentType) throws PluginInstallException
    {
        try
        {
            boolean isXml = connectIdentifier.isConnectAddOn(descriptorFile);
            Plugin plugin;

            UserProfile user = userManager.getRemoteUser();
            String username = user == null ? "" : user.getUsername();
            if (isXml)
            {
                //TODO: get rid of formatConverter when we go to capabilities
                Document doc = formatConverter.readFileToDoc(descriptorFile);

                plugin = connectInstaller.install(username, doc);
            }
            else
            {
                String json = Files.toString(descriptorFile, Charsets.UTF_8);
                plugin = connectInstaller.install(username, json);
            }

            return new PluginInstallResult(plugin);
        }
        catch (PluginInstallException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("Failed to install " + descriptorFile.getName() + ": " + e.getMessage(), e);
            throw new PluginInstallException("Unable to install connect add on. " + e.getMessage(),
                    Option.some("connect.remote.upm.install.exception"));
        }
    }

}
