package com.atlassian.labs.remoteapps.installer;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppUninstalledEvent;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.util.zip.ZipHandler;
import com.atlassian.labs.speakeasy.external.SpeakeasyBackendService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private final SpeakeasyBackendService speakeasyBackendService;

    private static final Logger log = LoggerFactory.getLogger(
            DefaultRemoteAppInstaller.class);

    @Autowired
    public DefaultRemoteAppInstaller(ConsumerService consumerService,
            RequestFactory requestFactory,
            PluginController pluginController,
            ApplicationProperties applicationProperties,
            ModuleGeneratorManager moduleGeneratorManager,
            EventPublisher eventPublisher,
            DescriptorValidator descriptorValidator,
            PluginAccessor pluginAccessor, SpeakeasyBackendService speakeasyBackendService)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.eventPublisher = eventPublisher;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
        this.speakeasyBackendService = speakeasyBackendService;
    }

    @Override
    public String install(final String username, final String registrationUrl,
            String registrationSecret, final KeyValidator keyValidator) throws
                                                                        PermissionDeniedException
    {
        /*!#start
        The installation process begins by constructing a GET to the Remote App to retrieve its
        registration
        XML file.  As part of that GET, the following query parameters will be passed along:
        <ul>
          <li><strong>key</strong> - The OAuth consumer key of the host application</li>
          <li><strong>publicKey</strong> - The OAuth public key of the host application,
          used to sign all future
            requests to the Remote App such as web hook POSTs, and Confluence macro retrieval
            queries.</li>
          <li><strong>baseUrl</strong> - The base URL of the host application,
          excluding the final slash.</li>
          <li><strong>description</strong> - The description of the host application for display
          purposes</li>
        </ul>
         */
        Consumer consumer = consumerService.getConsumer();
        final URI registrationUri = URI.create(
                encodeGetUrl(registrationUrl, ImmutableMap.of(
                        "key", consumer.getKey(),
                        "publicKey",
                        RSAKeys.toPemEncoding(consumer.getPublicKey()),
                        "baseUrl", applicationProperties.getBaseUrl(),
                        "description", consumer.getDescription())));

        log.info("Retrieving descriptor XML from '{}' by user '{}'", registrationUrl, username);
        Request request = requestFactory.createRequest(Request.MethodType.GET,
                registrationUri.toString());
        try
        {
            return (String) request.executeAndReturn(
                    new ReturningResponseHandler<Response, String>()
                    {
                        @Override
                        public String handle(Response response) throws ResponseException
                        {
                            /*!
                           If the request for the descriptor XML doesn't return with an HTTP 200
                           status code, an exception is thrown, resulting
                            in a error message to the user
                            */
                            if (response.getStatusCode() != 200)
                            {
                                throw new InstallationFailedException(
                                        "Missing registration url: " + response.getStatusCode());
                            }

                            /*!
                           The successfully returned XML descriptor is validated against the XML
                           schema.  If the incoming
                             document uses the XML schema namespaces, great, but if not,
                             the namespace is applied to the
                             document and the validation is then ran.  The XML schema can be
                             found at:
                             <pre>
                               https://HOST/rest/remoteapps/latest/installer/schema/remote-app
                             </pre>
                             Error pages are shown if the validation fails.
                            */
                            String descriptorXml = response.getResponseBodyAsString();
                            final Document document = descriptorValidator.parseAndValidate(
                                    registrationUrl, descriptorXml);

                            /*!
                           The app key, as defined in the registration XML document,
                           must be valid for the installation user.
                           This means, the app key must either not be used by an existing app,
                           or if it is, the installation
                           user must have the appropriate permissions to upgrade or modify that
                           app.  If either of these
                           criteria fail, the user is shown an error message.
                            */
                            final Element root = document.getRootElement();
                            final String pluginKey = root.attributeValue("key");
                            keyValidator.validate(pluginKey);

                            /*!
                           With the app key validated, the previous app with that key, if any,
                           are uninstalled.
                            */
                            if (pluginAccessor.getPlugin(pluginKey) != null)
                            {
                                uninstall(pluginKey);
                            }

                            /*!
                           The registration XML is then processed through second-level
                           validations to ensure the values
                           are valid for this host application.  Among these include,
                           <ul>
                             <li>The display-url property is checked to ensure it shares the same
                              stem as the registration URL </li>
                             <li>If any global modules such as Confluence macros are used,
                             the installation user should have the correct permission to install
                             global apps as the Remote App will then be treated as a global app
                             .</li>
                             <li>
                             <li>If any permissions are specified, the installation user must be
                             an administrator</li>
                           </ul>
                            */
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

                            /*!
                            Finally, the descriptor XML is transformed into an Atlassian OSGi
                            plugin artifact and installed into the host application.
                            */
                            Document pluginXml = transformDescriptorToPluginXml(username,
                                    registrationUrl, document);

                            /*!
                            To ensure the app shows up as a globally-enabled extension in Speakeasy,
                            the app is marked as such.  This is only temporary until the Remote
                            App plugin gets its own UI, likely integrated with the normal plugin
                            management UI.
                             */
                            if (!speakeasyBackendService.isGlobalExtension(pluginKey))
                            {
                                speakeasyBackendService.addGlobalExtension(pluginKey);
                            }

                            /*!
                            To create the final jar that will be installed into the plugin system,
                            The transformed plugin XML will be combined with an internationalization
                            properties file containing keys extracted out of the registration XML.
                             */
                            JarPluginArtifact jar = createJarPluginArtifact(pluginKey,
                                    registrationUri.getHost(), pluginXml, i18nMessages);

                            /*!
                            The registration process should only return once the Remote App has
                            successfully installed and started.
                            */
                            final CountDownLatch latch = new CountDownLatch(1);
                            Object startListener = new StartedListener(pluginKey, latch);
                            eventPublisher.register(startListener);


                            try
                            {
                                pluginController.installPlugins(jar);

                                if (!latch.await(10, TimeUnit.SECONDS))
                                {
                                    log.info("Remote app '{}' was not started successfully and is "
                                            + "disabled");
                                }
                            }
                            catch (InterruptedException e)
                            {
                                // ignore
                            }
                            finally
                            {
                                eventPublisher.unregister(startListener);
                            }

                            log.info("Registered app '{}' by '{}'", pluginKey, username);

                            eventPublisher.publish(new RemoteAppInstalledEvent(pluginKey));

                            return pluginKey;
                        }
                    });
        }

        /*!
        Any exceptions thrown in the installation process will result in a error message returned
         to the user
         */
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
            log.warn("Unable to install remote app from '{}' by user '{}'", registrationUrl,
                    username);
            Throwable ex = e.getCause() != null ? e.getCause() : e;
            throw new InstallationFailedException(ex);
        }
        /*!-helper methods */
    }


    @Override
    public void uninstall(String appKey) throws PermissionDeniedException
    {
        Plugin plugin = pluginAccessor.getPlugin(appKey);
        eventPublisher.publish(new RemoteAppUninstalledEvent(appKey));
        pluginController.uninstall(plugin);
    }

    private JarPluginArtifact createJarPluginArtifact(final String pluginKey,
            String host, final Document pluginXml, final Properties props)
    {
        return new JarPluginArtifact(
                ZipBuilder.buildZip("install-" + host, new ZipHandler()
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

    private static void attachResources(String pluginKey, Properties props,
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

    private Document transformDescriptorToPluginXml(String username,
            String registrationUrl, Document doc)
    {
        Element oldRoot = doc.getRootElement();

        final Element plugin = doc.getRootElement().addElement(
                "atlassian-plugin");
        plugin.detach();
        plugin.addAttribute("plugins-version", "2");
        plugin.addAttribute("key", getRequiredAttribute(oldRoot, "key"));
        plugin.addAttribute("name", getRequiredAttribute(oldRoot, "name"));
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
                        generator.convertDescriptor(
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
                                ApiScope.class.getPackage().getName());
        instructions.addElement("Remote-App").
                setText("installer;user=\"" + username + "\";date=\""
                        + System.currentTimeMillis() + "\"" +
                        ";registration-url=\"" + registrationUrl + "\"");

        plugin.add(oldRoot.detach());
        doc.setRootElement(plugin);

        return doc;
    }

    public static class StartedListener
    {
        private final String pluginKey;
        private final CountDownLatch latch;

        public StartedListener(String pluginKey, CountDownLatch latch)
        {
            this.pluginKey = pluginKey;
            this.latch = latch;
        }

        @EventListener
        public void onAppStart(RemoteAppStartedEvent event)
        {
            if (event.getRemoteAppKey().equals(pluginKey))
            {
                latch.countDown();
            }
        }
    }

    private class ValidateModuleHandler implements ModuleGeneratorManager.ModuleHandler
    {
        private final String registrationUrl;
        private final String username;
        private final Properties props;
        private final String pluginKey;

        public ValidateModuleHandler(String registrationUrl, String username,
                Properties props,
                String pluginKey)
        {
            this.registrationUrl = registrationUrl;
            this.username = username;
            this.props = props;
            this.pluginKey = pluginKey;
        }

        @Override
        public void handle(Element element, RemoteModuleGenerator generator)
        {
            generator.validate(element, registrationUrl, username);
            props.putAll(generator.getI18nMessages(pluginKey, element));
        }
    }
}
