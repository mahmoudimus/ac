package com.atlassian.labs.remoteapps.installer;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevelModuleDescriptor;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.util.zip.ZipHandler;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Properties;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.util.ServletUtils.encodeGetUrl;

/**
 * Handles the remote app installation dance
 */
@Component
public class DefaultRemoteAppInstaller implements RemoteAppInstaller
{
    private final ConsumerService consumerService;
    private final RequestFactory requestFactory;
    private final PluginController pluginController;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final ModuleGeneratorManager moduleGeneratorManager;
    private final EventPublisher eventPublisher;

    @Autowired
    public DefaultRemoteAppInstaller(ConsumerService consumerService,
                                     RequestFactory requestFactory,
                                     PluginController pluginController,
                                     ApplicationProperties applicationProperties, PermissionManager permissionManager,
                                     ModuleGeneratorManager moduleGeneratorManager, EventPublisher eventPublisher)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void install(final String username, final String registrationUrl, String registrationSecret) throws PermissionDeniedException
    {
        if (!permissionManager.canInstallRemoteApps(username))
        {
            throw new PermissionDeniedException("Unauthorized access by '" + username + "'");
        }
        Consumer consumer = consumerService.getConsumer();
        final URI registrationUri = URI.create(encodeGetUrl(registrationUrl, ImmutableMap.of(
                "key", consumer.getKey(),
                "publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()),
                "baseUrl", applicationProperties.getBaseUrl(),
                "description", consumer.getDescription())));

        Request request = requestFactory.createRequest(Request.MethodType.GET, registrationUri.toString());
        try
        {
            request.execute(new ResponseHandler()
            {
                @Override
                public void handle(Response response) throws ResponseException
                {
                    if (response.getStatusCode() != 200)
                    {
                        throw new InstallationFailedException("Missing registration url: " + response.getStatusCode());
                    }
                    String descriptorXml = response.getResponseBodyAsString();
                    final Document document = createDocument(descriptorXml);
                    Element root = document.getRootElement();
                    final String pluginKey = root.attributeValue("key");
                    String accessLevel = root.attributeValue("access-level");
                    final Properties props = new Properties();

                    try
                    {
                        moduleGeneratorManager.getApplicationTypeModuleGenerator().validate(root, registrationUrl);
                        moduleGeneratorManager.processDescriptor(root, new ModuleGeneratorManager.ModuleHandler()
                        {
                            @Override
                            public void handle(Element element, RemoteModuleGenerator generator)
                            {
                                generator.validate(element);
                                props.putAll(generator.getI18nMessages(pluginKey, element));
                            }
                        });
                    }
                    catch (PluginParseException ex)
                    {
                        throw new InstallationFailedException("Validation of the descriptor failed: " + ex.getMessage(), ex);
                    }
                    Document pluginXml = transformDescriptorToPluginXml(username, document);
                    JarPluginArtifact jar = createJarPluginArtifact(pluginKey, registrationUri.getHost(), pluginXml, props);
                    pluginController.installPlugins(jar);

                    // todo: retrieve the access level because it may have been modified.  Should be fixed as that sucks.
                    eventPublisher.publish(new RemoteAppInstalledEvent(pluginKey, root.attributeValue("access-level")));
                }
            });
        }
        catch (InstallationFailedException ex)
        {
            throw ex;
        }
        catch (Exception e)
        {
            Throwable ex = e.getCause() != null ? e.getCause() : e;

            throw new InstallationFailedException(ex);
        }
    }

    private Document createDocument(String xml)
    {
        Document doc;
        try
        {
            doc = new SAXReader().read(new StringReader(xml));
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Invalid remote app xml: \n" + xml, e);
        }
        return doc;
    }

    private JarPluginArtifact createJarPluginArtifact(final String pluginKey, String host, final Document pluginXml, final Properties props)
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + host, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                attachResources(pluginKey, props, pluginXml, builder);
                StringWriter out = new StringWriter();
                new XMLWriter(out).write(pluginXml);
                String descriptorXml = out.toString();
                builder.addFile("atlassian-plugin.xml", descriptorXml);
            }
        }));
    }

    private static void attachResources(String pluginKey, Properties props, Document pluginXml, ZipBuilder builder
    ) throws IOException
    {
        final StringWriter writer = new StringWriter();
        try
        {
            props.store(writer, "");
        }
        catch (IOException e)
        {
            // todo: do better
            throw new RuntimeException(e);
        }

        pluginXml.getRootElement().addElement("resource")
                .addAttribute("type", "i18n")
                .addAttribute("name", "i18n")
                .addAttribute("location", pluginKey.hashCode() + ".i18n");

        builder.addFile(pluginKey.hashCode() + "/i18n.properties", writer.toString());
    }

    private static Document transformDescriptorToPluginXml(String username, Document doc)
    {
        Element oldRoot = doc.getRootElement();

        Element plugin = doc.getRootElement().addElement("atlassian-plugin");
        plugin.detach();
        plugin.addAttribute("plugins-version", "2");
        plugin.addAttribute("key", getRequiredAttribute(oldRoot, "key"));
        plugin.addAttribute("name", getRequiredAttribute(oldRoot, "name"));
        Element info = plugin.addElement("plugin-info");
        info.addElement("version").setText(getRequiredAttribute(oldRoot, "version"));
        if (oldRoot.element("description") != null)
        {
            info.add(oldRoot.element("description").detach());
        }
        if (oldRoot.element("vendor") != null)
        {
            info.add(oldRoot.element("vendor").detach());
        }
        Element instructions = info.addElement("bundle-instructions");
        instructions
                .addElement("Import-Package")
                .setText(JiraProfileTabModuleGenerator.class.getPackage().getName() + ";resolution:=optional," +
                        AccessLevelModuleDescriptor.class.getPackage().getName());
        instructions.addElement("Remote-App").setText("installer;user=\"" + username + "\";date=\"" + System.currentTimeMillis() + "\"");


        plugin.add(oldRoot.detach());
        doc.setRootElement(plugin);

        return doc;
    }
}
