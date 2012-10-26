package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.remotable.plugin.OAuthLinkManager;
import com.atlassian.plugin.remotable.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Installs a remote plugin or plugin via retrieving content from a url.
 */
@Component
public final class DefaultRemotePluginInstaller implements RemotePluginInstaller
{
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final RemoteEventsHandler remoteEventsHandler;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemotePluginInstaller.class);

    @Autowired
    public DefaultRemotePluginInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory,
                                        PluginController pluginController,
                                        PluginAccessor pluginAccessor,
                                        OAuthLinkManager oAuthLinkManager,
                                        RemoteEventsHandler remoteEventsHandler)
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.remoteEventsHandler = remoteEventsHandler;
    }

    @Override
    public String install(final String username, final URI registrationUrl, Document document) throws PermissionDeniedException
    {
        try
        {
            final String pluginKey = getPluginKey(document);
            removeOldPlugin(pluginKey);

            final PluginArtifact pluginArtifact = getPluginArtifact(username, registrationUrl, document);

            pluginController.installPlugins(pluginArtifact).iterator().next();

            remoteEventsHandler.pluginInstalled(pluginKey);

            log.info("Registered app '{}' by '{}'", pluginKey, username);

            return pluginKey;
        }
        catch (PermissionDeniedException ex)
        {
            log.warn("Unable to install remote plugin from '{}' by user '{}' due to permission issues: {}",
                    new Object[]{registrationUrl, username, ex.getMessage()});
            log.debug("Installation failed due to permission issue", ex);
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            log.warn("Unable to install remote plugin from '{}' by user '{}' due to installation issue: {}",
                    new Object[]{registrationUrl, username, ex.getMessage()});
            log.debug("Installation failed due to installation issue", ex);
            throw ex;
        }
        catch (Exception e)
        {
            log.warn("Unable to install remote plugin from '{}' by user '{}'", registrationUrl, username);
            log.debug("Installation failed due to unknown issue", e);
            throw new InstallationFailedException(e.getCause() != null ? e.getCause() : e);
        }
    }

    private PluginArtifact getPluginArtifact(String username, URI registrationUrl, Document document)
    {
        if (document.getRootElement().attribute("plugins-version") != null)
        {
            return remotePluginArtifactFactory.create(registrationUrl, document, username);
        }
        else
        {
            throw new InstallationFailedException("Missing plugins-version");
        }
    }

    private String getPluginKey(Document document)
    {
        return document.getRootElement().attributeValue("key");
    }

    private void removeOldPlugin(String pluginKey)
    {
        final Plugin plugin = pluginAccessor.getPlugin(pluginKey);

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
                throw new PermissionDeniedException(pluginKey, "App key '" + pluginKey + "' is already associated with an OAuth link");
            }
        }
    }
}
