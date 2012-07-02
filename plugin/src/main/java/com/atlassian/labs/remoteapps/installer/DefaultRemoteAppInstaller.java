package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.FormatConverter;
import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.labs.remoteapps.util.uri.UriBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Properties;

import static com.atlassian.labs.remoteapps.util.EncodingUtils.encodeBase64;

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
    private final DescriptorValidator descriptorValidator;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final FormatConverter formatConverter;
    private final InstallerHelper installerHelper;

    private final BundleContext bundleContext;
    private static final Logger log = LoggerFactory.getLogger(
            DefaultRemoteAppInstaller.class);

    @Autowired
    public DefaultRemoteAppInstaller(ConsumerService consumerService,
            RequestFactory requestFactory,
            PluginController pluginController,
            ApplicationProperties applicationProperties,
            DescriptorValidator descriptorValidator,
            PluginAccessor pluginAccessor,
            OAuthLinkManager oAuthLinkManager, FormatConverter formatConverter,
            InstallerHelper installerHelper, BundleContext bundleContext)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.formatConverter = formatConverter;
        this.installerHelper = installerHelper;
        this.bundleContext = bundleContext;
    }

    @Override
    public String install(final String username, final URI registrationUrl,
            String registrationSecret, final boolean stripUnknownModules, final KeyValidator keyValidator) throws
                                                                        PermissionDeniedException
    {
        /*!#start
        The installation process begins by constructing a GET to the Remote App to retrieve its
        registration file.  As part of that GET, the following query parameters will be passed along:
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
          <li><strong>serverVersion</strong> - The version of the server requesting the registration</li>
          <li><strong>remoteappsVersion</strong> - The version of the Remote Apps framework</li>
        </ul>
         */
        final Consumer consumer = consumerService.getConsumer();
        final URI registrationUriWithParams = URI.create(
                new UriBuilder(Uri.fromJavaUri(registrationUrl)).addQueryParameters(ImmutableMap.<String, String>builder()
                        .put("key", consumer.getKey())
                        .put("publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()))
                        .put("serverVersion", applicationProperties.getBuildNumber())
                        .put("remoteappsVersion", (String) bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION))
                        .put("baseUrl", applicationProperties.getBaseUrl())
                        .put("description", consumer.getDescription())
                        .build()).toString());

        log.info("Retrieving descriptor from '{}' by user '{}'", registrationUrl, username);
        Request request = requestFactory.createRequest(Request.MethodType.GET,
                registrationUriWithParams.toString());

        /*!
        The registration secret is passed via the Authorization header using a custom scheme called
        'RemoteAppsRegistration'.  A custom scheme is used instead of another query parameter to
        ensure the secret can't be captured by server access logs.  The format of the Authorization
        header will look like this:
        <pre>
            RemoteAppsRegistration secret=BASE64\_ENCODED\_SECRET
        </pre>

        If a secret hasn't been supplied, the value will be blank.
         */
        String secretHeader = "RemoteAppsRegistration secret=" + encodeBase64(registrationSecret);
        request.addHeader("Authorization", secretHeader);
        try
        {
            return (String) request.executeAndReturn(
                    new ReturningResponseHandler<Response, String>()
                    {
                        @Override
                        public String handle(Response response) throws ResponseException
                        {
                            /*!
                           If the request for the descriptor doesn't return with an HTTP 200
                           status code, an exception is thrown, resulting
                            in a error message to the user
                            */
                            if (response.getStatusCode() != 200)
                            {
                                throw new InstallationFailedException(
                                        "Error in registration (" + response.getStatusCode() + ") - "
                                        + response.getStatusText());
                            }

                            /*!
                           The successfully returned descriptor is validated against the XML
                           schema.  While the descriptor is usually in an XML format, there is
                           *experimental* support for YAML and JSON if either the Content-Type
                           header is 'text/yaml' or 'application/json', respectively, or the
                           extension is '.yaml' or '.json'.

                           If the incoming document is in XML format and contains the XML
                           schema namespaces, great, but if not, the namespace is applied to the
                           document.
                           */
                            String descriptorText = response.getResponseBodyAsString();
                            String contentType = response.getHeader("Content-Type");
                            Document document = formatConverter.toDocument(
                                    registrationUrl.toString(), contentType, descriptorText);
                            
                           /*!
                           If the 'stripUnknownModules' flag is set to true, all unknown modules
                           will be removed from the document.  The default is false in order to give
                           early feedback of any problems, but there could be valid cases where
                           this should be set to 'true', such as a Remote App supporting multiple
                           applications.
                            */
                            if (stripUnknownModules)
                            {
                                installerHelper.detachUnknownModuleElements(document);
                            }

                           /*!
                           Regardless of the original format, the descriptor will be converted to
                           XML then validated.  The XML schema can be
                             found at:
                             <pre>
                               https://HOST/rest/remoteapps/latest/installer/schema/remote-app
                             </pre>
                             Error pages are shown if the validation fails.
                            */
                            descriptorValidator.validate(registrationUrl, document);

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
                            final Properties i18nMessages = installerHelper
                                    .validateAndGenerateMessages(root, pluginKey, registrationUrl,
                                            username);

                            /*!
                            Finally, the descriptor XML is transformed into an Atlassian OSGi
                            plugin descriptor file that contains general metadata about the app. The
                             contents of the plugin descriptor are derived from the remote app
                             descriptor.
                            */
                            Document pluginXml = installerHelper.generatePluginDescriptor(username,
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
                            JarPluginArtifact jar = installerHelper.createJarPluginArtifact(
                                    pluginKey,
                                    registrationUrl.getHost(), pluginXml, document, i18nMessages);

                            /*!
                            The registration process should only return once the Remote App has
                            successfully installed and started.
                            */
                            installerHelper.installRemoteAppPlugin(username, pluginKey, jar);

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
}
