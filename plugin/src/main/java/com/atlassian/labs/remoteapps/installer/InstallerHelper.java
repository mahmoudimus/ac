package com.atlassian.labs.remoteapps.installer;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.api.HttpResourceMounter;
import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.util.zip.ZipHandler;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginParseException;
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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;

@Component
public class InstallerHelper
{
    private final EventPublisher eventPublisher;
    private final PluginController pluginController;
    private final ModuleGeneratorManager moduleGeneratorManager;

    private static final Logger log = LoggerFactory.getLogger(InstallerHelper.class);

    @Autowired
    public InstallerHelper(EventPublisher eventPublisher, PluginController pluginController,
            ModuleGeneratorManager moduleGeneratorManager)
    {
        this.eventPublisher = eventPublisher;
        this.pluginController = pluginController;
        this.moduleGeneratorManager = moduleGeneratorManager;
    }

    public void installRemoteAppPlugin(String username, String pluginKey, JarPluginArtifact jar)
    {
        pluginController.installPlugins(jar);

        log.info("Registered app '{}' by '{}'", pluginKey, username);

        eventPublisher.publish(new RemoteAppInstalledEvent(pluginKey));
    }

    public JarPluginArtifact createJarPluginArtifact(final String pluginKey,
            String host, final Document pluginXml, final Document appXml, final Properties props)
    {
        return new JarPluginArtifact(
                ZipBuilder.buildZip("install-" + host, new ZipHandler()
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

    // fixme: this is temporary until UPM supports clear designation of remote apps
    public static String calculatePluginName(String name)
    {
        return name + " (Remote App)";
    }

    private void attachResources(String pluginKey, Properties props,
            Document pluginXml, ZipBuilder builder
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

        pluginXml.getRootElement().addElement("resource")
                .addAttribute("type", "i18n")
                .addAttribute("name", "i18n")
                .addAttribute("location", pluginKey.hashCode() + ".i18n");

        builder.addFile(pluginKey.hashCode() + "/i18n.properties",
                writer.toString());
    }

    public Document generatePluginDescriptor(String username,
            URI registrationUrl, Document doc)
    {
        Element oldRoot = doc.getRootElement();

        final Element plugin = DocumentHelper.createElement("atlassian-plugin");
        plugin.addAttribute("plugins-version", "2");
        plugin.addAttribute("key", getRequiredAttribute(oldRoot, "key"));
        plugin.addAttribute("name", calculatePluginName(getRequiredAttribute(oldRoot, "name")));
        Element info = plugin.addElement("plugin-info");
        info.addElement("version").setText(
                getRequiredAttribute(oldRoot, "version"));

        moduleGeneratorManager.processDescriptor(oldRoot,
                new ModuleGeneratorManager.ModuleHandler()
                {
                    @Override
                    public void handle(
                            Element element,
                            RemoteModuleGenerator generator)
                    {
                        generator.generatePluginDescriptor(
                                element,
                                plugin);
                    }
                });

        if (oldRoot.element("vendor") != null)
        {
            info.add(oldRoot.element("vendor").detach());
        }
        Element instructions = info.addElement("bundle-instructions");
        instructions
                .addElement("Import-Package")
                .setText(
                        JiraProfileTabModuleGenerator.class.getPackage().getName() +
                                ";resolution:=optional," +
                                "com.atlassian.jira.plugin.searchrequestview;resolution:=optional," +
                                HttpResourceMounter.class.getPackage().getName());
        instructions.addElement("Remote-App").
                setText("installer;user=\"" + username + "\";date=\""
                        + System.currentTimeMillis() + "\"" +
                        ";registration-url=\"" + registrationUrl + "\"");

        Document appDoc = DocumentHelper.createDocument();
        appDoc.setRootElement(plugin);

        return appDoc;
    }

    public void detachUnknownModuleElements(Document document)
    {
        Set<String> validModuleTypes = moduleGeneratorManager
                .getModuleGeneratorKeys();
        for (Element child : (List<Element>)document.getRootElement().elements())
        {
            if (!validModuleTypes.contains(child.getName()))
            {
                log.debug("Stripping unknown module '{}'", child.getName());
                child.detach();
            }
        }
    }

    public Properties validateAndGenerateMessages(Element root, String pluginKey,
            URI registrationUrl, String username)
    {
        final Properties i18nMessages = new Properties();
        try
        {
            moduleGeneratorManager.getApplicationTypeModuleGenerator()
                    .validate(root, registrationUrl, username);

            ValidateModuleHandler moduleValidator = new ValidateModuleHandler(
                    registrationUrl,
                    username,
                    i18nMessages,
                    pluginKey);
            moduleGeneratorManager.processDescriptor(root, moduleValidator);
        }
        catch (PluginParseException ex)
        {
            throw new InstallationFailedException(
                    "Validation of the descriptor failed: " + ex.getMessage(),
                    ex);
        }
        return i18nMessages;
    }

}
