package com.atlassian.labs.remoteapps.installer;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.descriptor.RemoteAppModuleDescriptor;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevelModuleDescriptor;
import com.atlassian.labs.remoteapps.event.RemoteAppUninstalledEvent;
import com.atlassian.labs.remoteapps.modules.GlobalModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.modules.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.util.zip.ZipHandler;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.*;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

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
    private final ModuleGeneratorManager moduleGeneratorManager;
    private final EventPublisher eventPublisher;
    private final DescriptorValidator descriptorValidator;
    private final PluginAccessor pluginAccessor;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemoteAppInstaller.class);

    @Autowired
    public DefaultRemoteAppInstaller(ConsumerService consumerService,
                                     RequestFactory requestFactory,
                                     PluginController pluginController,
                                     ApplicationProperties applicationProperties,
                                     ModuleGeneratorManager moduleGeneratorManager,
                                     EventPublisher eventPublisher,
                                     DescriptorValidator descriptorValidator,
                                     PluginAccessor pluginAccessor)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.eventPublisher = eventPublisher;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public String install(final String username, final String registrationUrl,
                          String registrationSecret, final KeyValidator keyValidator) throws PermissionDeniedException
    {
        Consumer consumer = consumerService.getConsumer();
        final URI registrationUri = URI.create(encodeGetUrl(registrationUrl, ImmutableMap.of(
                "key", consumer.getKey(),
                "publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()),
                "baseUrl", applicationProperties.getBaseUrl(),
                "description", consumer.getDescription())));

        log.info("Retrieving descriptor XML from '{}' by user '{}'", registrationUrl, username);
        Request request = requestFactory.createRequest(Request.MethodType.GET, registrationUri.toString());
        try
        {
            return (String)request.executeAndReturn(new ReturningResponseHandler<Response, String>()
            {
                @Override
                public String handle(Response response) throws ResponseException
                {
                    if (response.getStatusCode() != 200)
                    {
                        throw new InstallationFailedException("Missing registration url: " + response.getStatusCode());
                    }
                    String descriptorXml = response.getResponseBodyAsString();
                    final Document document = descriptorValidator.parseAndValidate(registrationUrl, descriptorXml);
                    final Element root = document.getRootElement();
                    final String pluginKey = root.attributeValue("key");
                    keyValidator.validate(pluginKey);

                    if (pluginAccessor.getPlugin(pluginKey) != null)
                    {
                        uninstall(pluginKey);
                    }
                    final Properties props = new Properties();
                    final AtomicReference<String> accessLevel = new AtomicReference<String>("user");
                    try
                    {
                        moduleGeneratorManager.getApplicationTypeModuleGenerator().validate(root, registrationUrl,
                                                                                            username);

                        moduleGeneratorManager.processDescriptor(root, new ModuleGeneratorManager.ModuleHandler()
                        {
                            @Override
                            public void handle(Element element, RemoteModuleGenerator generator)
                            {
                                generator.validate(element, registrationUrl, username);
                                if (generator.getClass().getAnnotation(GlobalModule.class) != null)
                                {
                                    // todo: restrict installation of global remote apps to admins
                                    accessLevel.set("global");
                                }
                                props.putAll(generator.getI18nMessages(pluginKey, element));
                            }
                        });
                    } catch (PluginParseException ex)
                    {
                        throw new InstallationFailedException("Validation of the descriptor failed: " + ex.getMessage(),
                                                              ex);
                    }
                    root.addAttribute("access-level", accessLevel.get());
                    Document pluginXml = transformDescriptorToPluginXml(username, document);
                    JarPluginArtifact jar = createJarPluginArtifact(pluginKey, registrationUri.getHost(), pluginXml,
                                                                    props);
                    pluginController.installPlugins(jar);

                    log.info("Registered app '{}' by '{}'", pluginKey, username);

                    eventPublisher.publish(new RemoteAppInstalledEvent(pluginKey, accessLevel.get()));

                    return pluginKey;
                }
            });
        }
        catch (PermissionDeniedException ex)
        {
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            throw ex;
        }
        catch (ResponseException e)
        {
            log.warn("Unable to retrieve registration XML from '{}' for user '{}' due to: {}",
                    new Object[]{registrationUrl, username, e.getMessage()});
            throw new InstallationFailedException(e);
        }
        catch (Exception e)
        {
            log.warn("Unable to install remote app from '{}' by user '{}'", registrationUrl, username);
            Throwable ex = e.getCause() != null ? e.getCause() : e;

            throw new InstallationFailedException(ex);
        }
    }

    @Override
    public void uninstall(String appKey) throws PermissionDeniedException
    {
        Plugin plugin = pluginAccessor.getPlugin(appKey);
        eventPublisher.publish(new RemoteAppUninstalledEvent(appKey));
        pluginController.uninstall(plugin);
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
            // shouldn't happen
            throw new RuntimeException(e);
        }

        pluginXml.getRootElement().addElement("resource")
                .addAttribute("type", "i18n")
                .addAttribute("name", "i18n")
                .addAttribute("location", pluginKey.hashCode() + ".i18n");

        builder.addFile(pluginKey.hashCode() + "/i18n.properties", writer.toString());
    }

    private Document transformDescriptorToPluginXml(String username, Document doc)
    {
        Element oldRoot = doc.getRootElement();

        final Element plugin = doc.getRootElement().addElement("atlassian-plugin");
        plugin.detach();
        plugin.addAttribute("plugins-version", "2");
        plugin.addAttribute("key", getRequiredAttribute(oldRoot, "key"));
        plugin.addAttribute("name", getRequiredAttribute(oldRoot, "name"));
        Element info = plugin.addElement("plugin-info");
        info.addElement("version").setText(getRequiredAttribute(oldRoot, "version"));

        moduleGeneratorManager.processDescriptor(oldRoot, new ModuleGeneratorManager.ModuleHandler()
        {
            @Override
            public void handle(Element element, RemoteModuleGenerator generator)
            {
                generator.convertDescriptor(element, plugin);
            }
        });

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
