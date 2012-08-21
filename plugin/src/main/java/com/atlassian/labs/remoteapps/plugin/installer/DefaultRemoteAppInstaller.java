package com.atlassian.labs.remoteapps.plugin.installer;

import com.atlassian.core.util.FileUtils;
import com.atlassian.labs.remoteapps.plugin.descriptor.DescriptorValidator;
import com.atlassian.labs.remoteapps.plugin.OAuthLinkManager;
import com.atlassian.labs.remoteapps.host.common.util.FormatConverter;
import com.atlassian.labs.remoteapps.spi.InstallationFailedException;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.plugin.util.uri.Uri;
import com.atlassian.labs.remoteapps.plugin.util.uri.UriBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.*;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static com.atlassian.labs.remoteapps.plugin.util.EncodingUtils.encodeBase64;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.readDocument;

/**
 * Installs a remote app or plugin via retrieving content from a url.
 */
@Component
public class DefaultRemoteAppInstaller implements RemoteAppInstaller
{
    private final RemoteAppDescriptorPluginArtifactFactory remoteAppDescriptorPluginArtifactFactory;
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final ConsumerService consumerService;
    private final RequestFactory requestFactory;
    private final ApplicationProperties applicationProperties;
    private final FormatConverter formatConverter;
    private final PluginController pluginController;
    private final DescriptorValidator descriptorValidator;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;


    private final BundleContext bundleContext;
    private static final Logger log = LoggerFactory.getLogger(DefaultRemoteAppInstaller.class);

    @Autowired
    public DefaultRemoteAppInstaller(RemoteAppDescriptorPluginArtifactFactory remoteAppDescriptorPluginArtifactFactory,
                                     RemotePluginArtifactFactory remotePluginArtifactFactory,
                                     ConsumerService consumerService,
                                     RequestFactory requestFactory,
                                     ApplicationProperties applicationProperties,
                                     FormatConverter formatConverter,
                                     PluginController pluginController,
                                     DescriptorValidator descriptorValidator,
                                     PluginAccessor pluginAccessor,
                                     OAuthLinkManager oAuthLinkManager,
                                     BundleContext bundleContext
    )
    {
        this.remoteAppDescriptorPluginArtifactFactory = remoteAppDescriptorPluginArtifactFactory;
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.applicationProperties = applicationProperties;
        this.formatConverter = formatConverter;
        this.pluginController = pluginController;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.bundleContext = bundleContext;
    }

    @Override
    public String install(final String username,
                          final URI registrationUrl,
                          String registrationSecret,
                          final boolean stripUnknownModules,
                          final KeyValidator keyValidator
    ) throws PermissionDeniedException
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
                new UriBuilder(Uri.fromJavaUri(registrationUrl)).addQueryParameters(
                        ImmutableMap.<String, String>builder()
                                    .put("key", consumer.getKey())
                                    .put("publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()))
                                    .put("serverVersion", applicationProperties.getBuildNumber())
                                    .put("remoteappsVersion", (String) bundleContext.getBundle()
                                                                                    .getHeaders()
                                                                                    .get(Constants.BUNDLE_VERSION))
                                    .put("baseUrl", applicationProperties.getBaseUrl())
                                    .put("description", consumer.getDescription())
                                    .build()).toString());

        log.info("Retrieving descriptor from '{}' by user '{}'", registrationUrl, username);
        Request request = requestFactory.createRequest(Request.MethodType.GET, registrationUriWithParams.toString());

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
            return (String) request.executeAndReturn(new ReturningResponseHandler<Response, String>()
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
                                "Error in registration (" + response.getStatusCode() + ") - " + response.getStatusText());
                    }

                    PluginArtifact pluginArtifact;
                    if (registrationUrl.getPath().endsWith(".jar"))
                    {
                        File pluginFile = getResponseAsTempFile(registrationUrl, response);
                        JarPluginArtifact originalArtifact = new JarPluginArtifact(pluginFile);
                        Document document = readDocument(originalArtifact.getResourceAsStream("atlassian-plugin.xml"));
                        prepareForInstallation(document, registrationUrl, keyValidator);

                        pluginArtifact = remotePluginArtifactFactory.create(registrationUrl, originalArtifact,
                                document, username);
                    }
                    else
                    {
                        Document document = formatConverter.toDocument(registrationUrl.toString(),
                                response.getHeader("Content-Type"),
                                response.getResponseBodyAsString());
                        prepareForInstallation(document, registrationUrl, keyValidator);

                        if (document.getRootElement().attribute("plugins-version") != null)
                        {
                            pluginArtifact = remotePluginArtifactFactory.create(registrationUrl,
                                    document, username);
                        }
                        else
                        {
                            pluginArtifact = remoteAppDescriptorPluginArtifactFactory.create(registrationUrl, document,
                                    username);
                        }
                    }

                    /*!
                    The registration process should only return once the Remote App has
                    successfully installed and started.
                    */
                    String pluginKey = pluginController.installPlugins(pluginArtifact).iterator().next();

                    log.info("Registered app '{}' by '{}'", pluginKey, username);

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
            log.warn("Unable to install remote app from '{}' by user '{}'", registrationUrl, username);
            Throwable ex = e.getCause() != null ? e.getCause() : e;
            throw new InstallationFailedException(ex);
        }
        /*!-helper methods */
    }

    private void prepareForInstallation(Document document, URI registrationUrl, KeyValidator keyValidator
    )
    {
        String pluginKey = validateDocument(registrationUrl, document, keyValidator);
        removeOldPlugin(pluginKey);
    }

    private void removeOldPlugin(String pluginKey)
    {
        /*!
                        The app key, as defined in the registration XML document,
                        must be valid for the installation user.
                        This means, the app key must either not be used by an existing app,
                        or if it is, the installation
                        user must have the appropriate permissions to upgrade or modify that
                        app.  If either of these
                        criteria fail, the user is shown an error message.
                        */
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
                throw new PermissionDeniedException(
                        "App key '" + pluginKey + "' is already associated with an OAuth link");
            }
        }
    }

    private String validateDocument(URI registrationUrl, Document document, KeyValidator keyValidator)
    {
        final String pluginKey = document.getRootElement().attributeValue("key");
        keyValidator.validatePermissions(pluginKey);
        descriptorValidator.validate(registrationUrl, document);
        return pluginKey;
    }

    private File getResponseAsTempFile(URI registrationUri, Response response)
    {
        String fileName = registrationUri.getPath().substring(registrationUri.getPath().lastIndexOf('/') + 1);
        InputStream in = null;
        try
        {
            File tmp = File.createTempFile("remote-app-install-", fileName);
            in = response.getResponseBodyAsStream();
            FileUtils.copyFile(in, tmp);
            return tmp;
        }
        catch (ResponseException e)
        {
            throw new InstallationFailedException("Unable to read from the response", e);
        }
        catch (IOException e)
        {
            throw new InstallationFailedException("Unable to create file from response", e);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }
}
