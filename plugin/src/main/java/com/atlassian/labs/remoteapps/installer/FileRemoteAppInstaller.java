package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.FormatConverter;
import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.plugin.*;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * Installs remote apps from a local file descriptor.  Only allowed in dev mode.
 */
@Component
public class FileRemoteAppInstaller implements RemoteAppInstaller
{
    private final PluginController pluginController;
    private final DescriptorValidator descriptorValidator;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final FormatConverter formatConverter;
    private final ApplicationProperties applicationProperties;
    private final InstallerHelper installerHelper;

    @Autowired
    public FileRemoteAppInstaller(PluginController pluginController,
            DescriptorValidator descriptorValidator, PluginAccessor pluginAccessor,
            OAuthLinkManager oAuthLinkManager, FormatConverter formatConverter,
            ApplicationProperties applicationProperties, InstallerHelper installerHelper)
    {
        this.pluginController = pluginController;
        this.installerHelper = installerHelper;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.formatConverter = formatConverter;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String install(String username, URI registrationUrl, String registrationSecret,
            boolean stripUnknownModules, KeyValidator keyValidator) throws
            PermissionDeniedException
    {
        if (!Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE))
        {
            throw new PermissionDeniedException("File descriptors are only accepted in dev mode");
        }

        String contentType = registrationUrl.getPath().endsWith(".json") ? "application/json" :
                             registrationUrl.getPath().endsWith(".yaml") ? "text/yaml" :
                                                                           "text/xml";

        File descriptorFile = new File(registrationUrl);
        if (!descriptorFile.exists())
        {
            throw new PermissionDeniedException("Descriptor out found: " + registrationUrl);
        }

        String descriptorText = null;
        try
        {
            descriptorText = FileUtils.readFileToString(descriptorFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read descriptor", e);
        }

        // this is still much too similar to DefaultRemoteAppInstaller, but leaving it as
        // the other class has the nice docco comments
        Document document = formatConverter.toDocument(
                registrationUrl.toString(), contentType, descriptorText);
        final Element root = document.getRootElement();
        final String pluginKey = root.attributeValue("key");
        URI displayUrl = URI.create(applicationProperties.getBaseUrl() + "/app/" + pluginKey);            // fix me
        root.addAttribute("display-url", displayUrl.toString());

        descriptorValidator.validate(registrationUrl, document);


        keyValidator.validatePermissions(pluginKey);
        Plugin plugin = pluginAccessor.getPlugin(pluginKey);

        if (plugin != null)
        {
            pluginController.uninstall(plugin);
        }
        else
        {
            if (oAuthLinkManager.isAppAssociated(pluginKey))
            {
                throw new PermissionDeniedException("App key '" + pluginKey
                        + "' is already associated with an OAuth link");
            }
        }


        final Properties i18nMessages = installerHelper.validateAndGenerateMessages(root, pluginKey, displayUrl, username);

        Document pluginXml = installerHelper.generatePluginDescriptor(username,
                registrationUrl, document);


        JarPluginArtifact jar = installerHelper.createJarPluginArtifact(pluginKey,
                registrationUrl.getHost(), pluginXml, document, i18nMessages);

        installerHelper.installRemoteAppPlugin(username, pluginKey, jar);

        return pluginKey;
    }
}
