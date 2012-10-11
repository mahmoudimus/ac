package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.core.util.FileUtils;
import com.atlassian.plugin.remotable.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugin.remotable.plugin.OAuthLinkManager;
import com.atlassian.plugin.remotable.host.common.util.FormatConverter;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.*;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
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

import static com.atlassian.plugin.remotable.plugin.util.EncodingUtils.encodeBase64;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.readDocument;

/**
 * Installs a remote plugin or plugin via retrieving content from a url.
 */
@Component
public class DefaultRemotePluginInstaller implements RemotePluginInstaller
{
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final ConsumerService consumerService;
    private final RequestFactory requestFactory;
    private final ApplicationProperties applicationProperties;
    private final FormatConverter formatConverter;
    private final PluginController pluginController;
    private final DescriptorValidator descriptorValidator;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final ProductAccessor productAccessor;


    private final BundleContext bundleContext;
    private static final Logger log = LoggerFactory.getLogger(DefaultRemotePluginInstaller.class);

    @Autowired
    public DefaultRemotePluginInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory,
                                        ConsumerService consumerService,
                                        RequestFactory requestFactory,
                                        ApplicationProperties applicationProperties,
                                        FormatConverter formatConverter,
                                        PluginController pluginController,
                                        DescriptorValidator descriptorValidator,
                                        PluginAccessor pluginAccessor,
                                        OAuthLinkManager oAuthLinkManager,
                                        ProductAccessor productAccessor,
                                        BundleContext bundleContext
    )
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.applicationProperties = applicationProperties;
        this.formatConverter = formatConverter;
        this.pluginController = pluginController;
        this.descriptorValidator = descriptorValidator;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.productAccessor = productAccessor;
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
        final Consumer consumer = consumerService.getConsumer();
        final URI registrationUriWithParams = URI.create(
                new UriBuilder(Uri.fromJavaUri(registrationUrl)).addQueryParameters(
                        ImmutableMap.<String, String>builder()
                                    .put("key", consumer.getKey())
                                    .put("publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()))
                                    .put("serverVersion", applicationProperties.getBuildNumber())
                                    .put("pluginsVersion", (String) bundleContext.getBundle()
                                                                                    .getHeaders()
                                                                                    .get(Constants.BUNDLE_VERSION))
                                    .put("baseUrl", applicationProperties.getBaseUrl())
                                    .put("productType", productAccessor.getKey())
                                    .put("description", consumer.getDescription())
                                    .build()).toString());

        log.info("Retrieving descriptor from '{}' by user '{}'", registrationUrl, username);
        Request request = requestFactory.createRequest(Request.MethodType.GET, registrationUriWithParams.toString());

        String secretHeader = "RemotePluginRegistration secret=" + encodeBase64(registrationSecret);
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
                            throw new InstallationFailedException("Missing plugins-version");
                        }
                    }

                    /*!
                    The registration process should only return once the remote plugin has
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
            log.warn("Unable to install remote plugin from '{}' by user '{}'", registrationUrl, username);
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
                throw new PermissionDeniedException(pluginKey,
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
            File tmp = File.createTempFile("remote-plugin-install-", fileName);
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
