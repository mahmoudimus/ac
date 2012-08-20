package com.atlassian.labs.remoteapps.plugin.installer;

import com.atlassian.labs.remoteapps.api.service.HttpResourceMounter;
import com.atlassian.labs.remoteapps.plugin.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.plugin.OAuthLinkManager;
import com.atlassian.labs.remoteapps.plugin.descriptor.DescriptorValidator;
import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.plugin.module.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.plugin.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.plugin.util.zip.ZipHandler;
import com.atlassian.labs.remoteapps.spi.InstallationFailedException;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.plugin.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Properties;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredAttribute;

/**
 *
 */
@Component
public class RemoteAppDescriptorPluginArtifactFactory
{
    private final ModuleGeneratorManager moduleGeneratorManager;


    private static final Logger log = LoggerFactory.getLogger(RemoteAppDescriptorPluginArtifactFactory.class);

    @Autowired
    public RemoteAppDescriptorPluginArtifactFactory(ModuleGeneratorManager moduleGeneratorManager)
    {
        this.moduleGeneratorManager = moduleGeneratorManager;
    }

    public PluginArtifact create(URI registrationUrl, Document document, String username)
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        Element root = document.getRootElement();

        /*!
The registration XML is then processed through second-level
validations to ensure the values
are valid for this host application.  Among these include,
<ul>
<li>The display-url property is checked to ensure it shares the same
stem as the registration URL </li>
<li>If any permissions are specified, the installation user must be
an administrator</li>
</ul>
*/
        final Properties i18nMessages = validateAndGenerateMessages(root, pluginKey, registrationUrl, username);

        /*!
Finally, the descriptor XML is transformed into an Atlassian OSGi
plugin descriptor file that contains general metadata about the app. The
contents of the plugin descriptor are derived from the remote app
descriptor.
*/
        Document pluginXml = generatePluginDescriptor(username, registrationUrl, document);


        /*!
To create the final jar that will be installed into the plugin system,
several generated files are combined into one plugin artifact.
This artifact will contain:
1. atlassian-remote-app.xml - The remote app descriptor
2. atlassian-plugin.xml - Metadata about the app used to display the app
in the plugin system.  The contents of this file are derived from the
app descriptor.
3. META-INF/spring/remoteapps-loader.xml - A Spring XML configuration file
that references the loader service from the remote apps plugin, used
to kick off the descriptor generation step before the app-plugin is
finished loading.
4. i18n.properties - An internationalization properties file containing
keys extracted out of the app descriptor XML.
*/
        return createJarPluginArtifact(pluginKey, registrationUrl.getHost(), pluginXml, document, i18nMessages);
    }

    public JarPluginArtifact createJarPluginArtifact(final String pluginKey,
                                                     String host,
                                                     final Document pluginXml,
                                                     final Document appXml,
                                                     final Properties props
    )
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + host, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                attachResources(pluginKey, props, pluginXml, builder);
                builder.addFile("atlassian-plugin.xml", pluginXml);
                builder.addFile("atlassian-remote-app.xml", appXml);
            }
        }));
    }

    public Properties validateAndGenerateMessages(Element root, String pluginKey, URI registrationUrl, String username)
    {
        final Properties i18nMessages = new Properties();
        try
        {
            moduleGeneratorManager.getApplicationTypeModuleGenerator().validate(root, registrationUrl, username);

            ValidateModuleHandler moduleValidator = new ValidateModuleHandler(registrationUrl, username, i18nMessages,
                    pluginKey);
            moduleGeneratorManager.processDescriptor(root, moduleValidator);
        }
        catch (PluginParseException ex)
        {
            throw new InstallationFailedException("Validation of the descriptor failed: " + ex.getMessage(), ex);
        }
        return i18nMessages;
    }

    private void attachResources(String pluginKey, Properties props, Document pluginXml, ZipBuilder builder
    ) throws IOException
    {
        final StringWriter writer = new StringWriter();
        try
        {
            props.store(writer, "");
        }
        catch (IOException e)
        {
            // shouldn't happen
            throw new RuntimeException(e);
        }

        pluginXml.getRootElement()
                 .addElement("resource")
                 .addAttribute("type", "i18n")
                 .addAttribute("name", "i18n")
                 .addAttribute("location", pluginKey.hashCode() + ".i18n");

        builder.addFile(pluginKey.hashCode() + "/i18n.properties", writer.toString());
    }

    // fixme: this is temporary until UPM supports clear designation of remote apps
    public static String calculatePluginName(String name)
    {
        return name + " (Remote App)";
    }

    private Document generatePluginDescriptor(String username, URI registrationUrl, Document doc)
    {
        Element oldRoot = doc.getRootElement();

        final Element plugin = DocumentHelper.createElement("atlassian-plugin");
        plugin.addAttribute("plugins-version", "2");
        plugin.addAttribute("key", getRequiredAttribute(oldRoot, "key"));
        plugin.addAttribute("name", calculatePluginName(getRequiredAttribute(oldRoot, "name")));
        Element info = plugin.addElement("plugin-info");
        info.addElement("version").setText(getRequiredAttribute(oldRoot, "version"));

        moduleGeneratorManager.processDescriptor(oldRoot, new ModuleGeneratorManager.ModuleHandler()
        {
            @Override
            public void handle(Element element, RemoteModuleGenerator generator)
            {
                generator.generatePluginDescriptor(element, plugin);
            }
        });

        if (oldRoot.element("vendor") != null)
        {
            info.add(oldRoot.element("vendor").detach());
        }
        Element instructions = info.addElement("bundle-instructions");
        instructions.addElement("Import-Package").setText(JiraProfileTabModuleGenerator.class.getPackage().getName() +
                ";resolution:=optional," +
                "com.atlassian.jira.plugin.searchrequestview;resolution:=optional," +
                HttpResourceMounter.class.getPackage().getName());
        instructions.addElement("Remote-App").
                setText("installer;user=\"" + username + "\";date=\"" + System.currentTimeMillis() + "\"" +
                        ";registration-url=\"" + registrationUrl + "\"");

        Document appDoc = DocumentHelper.createDocument();
        appDoc.setRootElement(plugin);

        return appDoc;
    }
}
