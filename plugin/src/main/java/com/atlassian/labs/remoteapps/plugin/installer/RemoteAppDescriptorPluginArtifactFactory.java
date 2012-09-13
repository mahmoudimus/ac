package com.atlassian.labs.remoteapps.plugin.installer;

import com.atlassian.labs.remoteapps.api.service.HttpResourceMounter;
import com.atlassian.labs.remoteapps.plugin.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.plugin.module.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.plugin.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.plugin.util.zip.ZipHandler;
import com.atlassian.labs.remoteapps.spi.InstallationFailedException;
import com.atlassian.plugin.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.tools.jar.resources.jar;

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

        validate(root, registrationUrl, username);

        Document pluginXml = generatePluginDescriptor(username, registrationUrl, document);

        return createJarPluginArtifact(pluginKey, registrationUrl.getHost(), pluginXml, document);
    }

    public JarPluginArtifact createJarPluginArtifact(final String pluginKey,
                                                     String host,
                                                     final Document pluginXml,
                                                     final Document appXml
    )
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + host, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", pluginXml);
                builder.addFile("atlassian-remote-app.xml", appXml);
            }
        }));
    }

    public void validate(Element root, URI registrationUrl, String username)
    {
        try
        {
            moduleGeneratorManager.getApplicationTypeModuleGenerator().validate(root, registrationUrl, username);

            ValidateModuleHandler moduleValidator = new ValidateModuleHandler(registrationUrl, username);
            moduleGeneratorManager.processDescriptor(root, moduleValidator);
        }
        catch (PluginParseException ex)
        {
            throw new InstallationFailedException("Validation of the descriptor failed: " + ex.getMessage(), ex);
        }
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
