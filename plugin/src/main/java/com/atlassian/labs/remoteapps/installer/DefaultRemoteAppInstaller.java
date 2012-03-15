package com.atlassian.labs.remoteapps.installer;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
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
    private final OAuthLinkManager oAuthLinkManager;

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
            PluginAccessor pluginAccessor,
            OAuthLinkManager oAuthLinkManager)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.eventPublisher = eventPublisher;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
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
          <li><strong>registrationSecret</strong> - The registration secret provided in the registration form, if any.  This is used to secure app registrations on the app side and is not stored or used again.</li>
        </ul>
         */
        Consumer consumer = consumerService.getConsumer();
        final URI registrationUri = URI.create(
                encodeGetUrl(registrationUrl, ImmutableMap.of(
                        "key", consumer.getKey(),
                        "publicKey",
                        RSAKeys.toPemEncoding(consumer.getPublicKey()),
                        "baseUrl", applicationProperties.getBaseUrl(),
                        "description", consumer.getDescription(),
                        "registrationSecret", registrationSecret != null ? registrationSecret : "")));

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
                            keyValidator.validatePermissions(pluginKey);
                            Plugin plugin = pluginAccessor.getPlugin(pluginKey);

                            /*!
                           With the app key validated for the user, the previous app with that key,
                           if any, is uninstalled.
                            */

                            if (plugin != null)
                            {
                                pluginController.uninstall(plugin);
                            }
                            else
                            {
                                /*!
                                 However, if there is no previous app, then the app key is checked
                                 to ensure it doesn't already exist as a OAuth client key.  This
                                 prevents a malicious app that uses a key from an existing oauth
                                 link from getting that link removed when the app is uninstalled.
                                */
                                if (oAuthLinkManager.isAppAssociated(pluginKey))
                                {
                                    throw new PermissionDeniedException("App key '" + pluginKey
                                            + "' is already associated with an OAuth link");
                                }
                            }
                            
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
                            plugin descriptor file that contains general metadata about the app. The
                             contents of the plugin descriptor are derived from the remote app
                             descriptor.
                            */
                            Document pluginXml = generatePluginDescriptor(username,
                                    registrationUrl, document);


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
                            JarPluginArtifact jar = createJarPluginArtifact(pluginKey,
                                    registrationUri.getHost(), pluginXml, document, i18nMessages);

                            /*!
                            The registration process should only return once the Remote App has
                            successfully installed and started.
                            */
                            final CountDownLatch latch = new CountDownLatch(1);
                            StartedListener startListener = new StartedListener(pluginKey, latch);
                            eventPublisher.register(startListener);


                            try
                            {
                                pluginController.installPlugins(jar);

                                Exception cause = startListener.getFailedCause();
                                if (!latch.await(10, TimeUnit.SECONDS)
                                        || cause != null)
                                {
                                    log.info("Remote app '{}' was not started successfully and is "
                                            + "disabled due to: {}", pluginKey,
                                            cause);
                                    throw new InstallationFailedException("Error starting app: "
                                            + cause.getMessage(),
                                            cause);
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

    private JarPluginArtifact createJarPluginArtifact(final String pluginKey,
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
                        builder.addFile("META-INF/spring/remoteapps-loader.xml", getClass().getResourceAsStream("remoteapps-loader.xml"));
                        builder.addFile("atlassian-remote-app.xml", appXml);
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

    private Document generatePluginDescriptor(String username,
            String registrationUrl, Document doc)
    {
        Element oldRoot = doc.getRootElement();

        final Element plugin = DocumentHelper.createElement("atlassian-plugin");
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
                                DescriptorGenerator.class.getPackage().getName());
        instructions.addElement("Remote-App").
                setText("installer;user=\"" + username + "\";date=\""
                        + System.currentTimeMillis() + "\"" +
                        ";registration-url=\"" + registrationUrl + "\"");

        Document appDoc = DocumentHelper.createDocument();
        appDoc.setRootElement(plugin);

        return appDoc;
    }

    public static class StartedListener
    {
        private final String pluginKey;
        private final CountDownLatch latch;

        private volatile Exception cause;

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

        @EventListener
        public void onAppStartFailed(RemoteAppStartFailedEvent event)
        {
            if (event.getRemoteAppKey().equals(pluginKey))
            {
                cause = event.getCause();
                latch.countDown();
            }
        }
        public Exception getFailedCause()
        {
            return cause;
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
