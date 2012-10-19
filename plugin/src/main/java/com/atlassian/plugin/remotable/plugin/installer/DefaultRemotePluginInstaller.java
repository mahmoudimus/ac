package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.*;
import com.atlassian.plugin.remotable.plugin.OAuthLinkManager;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.event.RemotePluginInstalledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.readDocument;

/**
 * Installs a remote plugin or plugin via retrieving content from a url.
 */
@Component
public class DefaultRemotePluginInstaller implements RemotePluginInstaller
{
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final ProductAccessor productAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;
    private final ConsumerService consumerService;
    private final EventPublisher eventPublisher;


    private static final Logger log = LoggerFactory.getLogger(DefaultRemotePluginInstaller.class);

    @Autowired
    public DefaultRemotePluginInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory,
            PluginController pluginController,
            PluginAccessor pluginAccessor,
            ProductAccessor productAccessor, OAuthLinkManager oAuthLinkManager,
            ApplicationProperties applicationProperties, BundleContext bundleContext,
            ConsumerService consumerService, EventPublisher eventPublisher)
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.productAccessor = productAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.applicationProperties = applicationProperties;
        this.bundleContext = bundleContext;
        this.consumerService = consumerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String install(final String username,
            final URI registrationUrl,
            Document document
    ) throws PermissionDeniedException
    {
        try
        {

            PluginArtifact pluginArtifact;
            String pluginKey = document.getRootElement().attributeValue("key");
            removeOldPlugin(pluginKey);

            if (document.getRootElement().attribute("plugins-version") != null)
            {
                pluginArtifact = remotePluginArtifactFactory.create(registrationUrl,
                        document, username);
            }
            else
            {
                throw new InstallationFailedException("Missing plugins-version");
            }
            pluginController.installPlugins(pluginArtifact).iterator().next();

            RemotePluginInstalledEvent event = new RemotePluginInstalledEvent(pluginKey, ImmutableMap.<String,Object>builder()
                .put("clientKey", consumerService.getConsumer().getKey())
                .put("publicKey", RSAKeys.toPemEncoding(consumerService.getConsumer().getPublicKey()))
                .put("serverVersion", applicationProperties.getBuildNumber())
                .put("pluginsVersion", (String) bundleContext.getBundle()
                    .getHeaders()
                    .get(Constants.BUNDLE_VERSION))
                .put("baseUrl", applicationProperties.getBaseUrl())
                .put("productType", productAccessor.getKey())
                .put("description", consumerService.getConsumer().getDescription())
                .build());
            eventPublisher.publish(event);

            log.info("Registered app '{}' by '{}'", pluginKey, username);

            return pluginKey;
        }
        catch (PermissionDeniedException ex)
        {
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            throw ex;
        }
        catch (Exception e)
        {
            log.warn("Unable to install remote plugin from '{}' by user '{}'", registrationUrl,
                    username);
            Throwable ex = e.getCause() != null ? e.getCause() : e;
            throw new InstallationFailedException(ex);
        }
    }

    private void removeOldPlugin(String pluginKey)
    {
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
}
