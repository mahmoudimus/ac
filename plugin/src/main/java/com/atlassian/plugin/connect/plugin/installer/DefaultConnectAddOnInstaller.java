package com.atlassian.plugin.connect.plugin.installer;

import java.util.Set;

import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultConnectAddOnInstaller implements ConnectAddOnInstaller
{
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final RemoteEventsHandler remoteEventsHandler;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final ConnectAddonToPluginFactory addonToPluginFactory;
    private final ConnectAddonManager connectAddonManager;

    private static final Logger log = LoggerFactory.getLogger(DefaultConnectAddOnInstaller.class);

    @Autowired
    public DefaultConnectAddOnInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory,
                                        PluginController pluginController,
                                        PluginAccessor pluginAccessor,
                                        OAuthLinkManager oAuthLinkManager,
                                        RemoteEventsHandler remoteEventsHandler,
                                        ConnectAddonBeanFactory connectAddonBeanFactory,
                                        ConnectAddonToPluginFactory addonToPluginFactory, ConnectAddonManager connectAddonManager)
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.remoteEventsHandler = remoteEventsHandler;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
        this.addonToPluginFactory = addonToPluginFactory;
        this.connectAddonManager = connectAddonManager;
    }

    @Override
    public Plugin install(final String username, final Document document) throws PluginInstallException
    {
        String pluginKey = getPluginKey(document);
        removeOldPlugin(pluginKey);

        final PluginArtifact pluginArtifact = getPluginArtifact(username, document);

        Plugin installedPlugin = installPlugin(pluginArtifact, pluginKey, username);

        try
        {
            remoteEventsHandler.pluginInstalled(pluginKey);
        }
        catch (PluginInstallException e)
        {
            log.error("An exception occurred while installing the plugin '[" + installedPlugin.getKey() + "]. Uninstalling...", e);
            pluginController.uninstall(installedPlugin);
            throw e;
        }

        return installedPlugin;
    }

    @Override
    public Plugin install(String username, String jsonDescriptor) throws PluginInstallException
    {
        String pluginKey = null;
        Plugin addonPluginWrapper = null;
        ConnectAddonBean addOn = null;
        
        long startTime = System.currentTimeMillis();

        try
        {
            //until we ensure we no longer have xml or mirror plugins, we need to call removeOldPlugin, which is why we marshal here just to get the plugin key
            ConnectAddonBean nonValidatedAddon = connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptor);
            
            pluginKey = nonValidatedAddon.getKey();
            
            if(nonValidatedAddon.getModules().isEmpty())
            {
                throw new PluginInstallException("Unable to install connect add on because it has no modules defined",
                        Option.some("connect.install.error.no.modules"));
            }
            
            removeOldPlugin(pluginKey);
        
            addOn = connectAddonManager.installConnectAddon(jsonDescriptor);
            connectAddonManager.enableConnectAddon(addOn.getKey());

            addonPluginWrapper = addonToPluginFactory.create(addOn);

            addonPluginWrapper.enable();

        }
        catch(PluginInstallException e)
        {
            if (null != pluginKey)
            {
                log.error("An exception occurred while installing the plugin '[" + pluginKey + "]. Uninstalling...", e);
                connectAddonManager.uninstallConnectAddonQuietly(pluginKey);
            }
            throw e;
        }
        catch (Exception e)
        {
            if (null != pluginKey)
            {
                log.error("An exception occurred while installing the plugin '[" + pluginKey + "]. Uninstalling...", e);
                connectAddonManager.uninstallConnectAddonQuietly(pluginKey);
            }
            throw new PluginInstallException(e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis();

        log.info("Connect add-on installed in " + (endTime - startTime) + "ms");

        return addonPluginWrapper;
    }

    private Plugin installPlugin(PluginArtifact pluginArtifact, String pluginKey, String username)
    {
        Plugin installedPlugin;
        try
        {
            Set<String> pluginKeys = pluginController.installPlugins(pluginArtifact);
            if (pluginKeys.size() == 1)
            {
                final String installedKey = pluginKeys.iterator().next();
                final Plugin plugin = pluginAccessor.getPlugin(installedKey);

                // a dodgy plugin artifact can result in an UnloadablePlugin: it has a key but is not loaded
                // so if you try to use that key to find a loaded plugin then you get nothing... boom.
                // e.g.: atlassian-plugin.xml contains multiple <webhook> entities with the same key.
                if (null == plugin)
                {
                    throw new InstallationFailedException(String.format("Plugin '%s' is did not load: check the application logs for errors", installedKey));
                }

                WaitUntil.invoke(new WaitUntil.WaitCondition()
                {
                    public boolean isFinished()
                    {
                        for (ModuleDescriptor desc : plugin.getModuleDescriptors())
                        {
                            if (!pluginAccessor.isPluginModuleEnabled(
                                    desc.getCompleteKey()) && desc instanceof UnrecognisedModuleDescriptor)
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                    public String getWaitMessage()
                    {
                        return "Waiting for all module descriptors to be resolved and enabled";
                    }
                });
                if (!pluginAccessor.isPluginEnabled(plugin.getKey()))
                {
                    String cause = "Plugin didn't install correctly";
                    for (ModuleDescriptor descriptor : plugin.getModuleDescriptors())
                    {
                        if (descriptor instanceof UnloadableModuleDescriptor)
                        {
                            cause = ((UnloadableModuleDescriptor) descriptor).getErrorText();
                            break;
                        }
                    }
                    throw new RuntimeException(cause);
                }
                else
                {
                    installedPlugin = plugin;
                }
            }
            else
            {
                throw new RuntimeException("Plugin didn't install correctly", null);
            }

            log.info("Registered app '{}' by '{}'", pluginKey, username);

            return installedPlugin;
        }
        catch (PermissionDeniedException ex)
        {
            log.warn("Unable to install remote plugin '{}' by user '{}' due to permission issues: {}",
                    new Object[]{pluginKey, username, ex.getMessage()});
            log.debug("Installation failed due to permission issue", ex);
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            log.warn("Unable to install remote plugin '{}' by user '{}' due to installation issue: {}",
                    new Object[]{pluginKey, username, ex.getMessage()});
            log.debug("Installation failed due to installation issue", ex);
            throw ex;
        }
        catch (PluginInstallException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.warn("Unable to install remote plugin '{}' by user '{}'", pluginKey, username);
            log.debug("Installation failed due to unknown issue", e);
            throw new InstallationFailedException(e.getCause() != null ? e.getCause() : e);
        }
    }

    private PluginArtifact getPluginArtifact(String username, Document document)
    {
        if (document.getRootElement().attribute("plugins-version") != null)
        {
            return remotePluginArtifactFactory.create(document, username);
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
        else if(connectAddonManager.hasDescriptor(pluginKey))
        {
            connectAddonManager.uninstallConnectAddonQuietly(pluginKey);
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
