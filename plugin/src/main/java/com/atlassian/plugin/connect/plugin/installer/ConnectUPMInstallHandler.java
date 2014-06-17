package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.atlassian.plugin.connect.plugin.descriptor.util.FormatConverter;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.atlassian.upm.spi.PluginInstallResult;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;

/**
 * @since 1.0
 */
@ExportAsService(PluginInstallHandler.class)
@Named
public class ConnectUPMInstallHandler implements PluginInstallHandler
{
    private static final Logger log = LoggerFactory.getLogger(ConnectUPMInstallHandler.class);

    @XmlDescriptor
    private final LegacyAddOnIdentifierService connectIdentifier;
    private final ConnectAddOnInstaller connectInstaller;
    private final UserManager userManager;
    private final FormatConverter formatConverter;
    private final JsonDescriptorValidator jsonDescriptorValidator;

    @Inject
    public ConnectUPMInstallHandler(LegacyAddOnIdentifierService connectIdentifier,
            ConnectAddOnInstaller connectInstaller, UserManager userManager, FormatConverter formatConverter,
            JsonDescriptorValidator jsonDescriptorValidator)
    {
        this.connectIdentifier = connectIdentifier;
        this.connectInstaller = connectInstaller;
        this.userManager = userManager;
        this.formatConverter = formatConverter;
        this.jsonDescriptorValidator = jsonDescriptorValidator;
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
                canInstall = jsonDescriptorValidator.isConnectJson(json, isJsonContentType(descriptorFile, contentType));

                if (!canInstall)
                {
                    log.error("The given plugin descriptor is not a valid connect json file");
                }
            }
            catch (IOException e)
            {
                log.error("Cannot load descriptor " + descriptorFile.getName(), e);
                canInstall = false;
            }
        }

        //TODO: if we have a json validation error and we can determine an error lifecycle url, we need to post the error message to the remote

        return canInstall;
    }

    private static boolean isJsonContentType(File descriptorFile, Option<String> contentType)
    {
        return matchesContentType(contentType, "application/json", "text/json")
                || descriptorFile.getName().toLowerCase().endsWith(".json");
    }

    private static boolean matchesContentType(Option<String> actualContentType, String... desiredContentType)
    {
        for (String contentType : actualContentType)
        {
            for (String desired : desiredContentType)
            {
                if (contentType.equals(desired) || contentType.startsWith(desired + ";"))
                {
                    return true;
                }
            }
        }
        return false;
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

                XmlDescriptorExploder.notifyAndExplode(null == plugin ? null : plugin.getKey());
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
            // pretty sure if we end up here Connect has done something wrong, not the add-on, so let's describe it as
            // an internal error and recommend contacting Atlassian support.
            log.error("Failed to install " + descriptorFile.getName() + ": " + e.getMessage(), e);
            Option<String> i18nKey = Option.some("connect.remote.upm.install.internal.error");
            throw new PluginInstallException("Unable to install connect add on. " + e.getMessage(),i18nKey);
        }
    }

}
