package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.event.ConnectEventHandler;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.upm.spi.PluginInstallException;
import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultConnectAddOnInstaller implements ConnectAddOnInstaller
{
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final OAuthLinkManager oAuthLinkManager;
    private final RemoteEventsHandler remoteEventsHandler;
    private final BeanToModuleRegistrar beanToModuleRegistrar;
    private final BundleContext bundleContext;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectDescriptorRegistry connectDescriptorRegistry;
    private final ConnectEventHandler connectEventHandler;
    private final SharedSecretService sharedSecretService;

    private static final Logger log = LoggerFactory.getLogger(DefaultConnectAddOnInstaller.class);

    @Autowired
    public DefaultConnectAddOnInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory, PluginController pluginController, PluginAccessor pluginAccessor, OAuthLinkManager oAuthLinkManager, RemoteEventsHandler remoteEventsHandler, BeanToModuleRegistrar beanToModuleRegistrar, BundleContext bundleContext, ConnectApplinkManager connectApplinkManager, ConnectDescriptorRegistry connectDescriptorRegistry, ConnectEventHandler connectEventHandler, SharedSecretService sharedSecretService)
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.remoteEventsHandler = remoteEventsHandler;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.bundleContext = bundleContext;
        this.connectApplinkManager = connectApplinkManager;
        this.connectDescriptorRegistry = connectDescriptorRegistry;
        this.connectEventHandler = connectEventHandler;
        this.sharedSecretService = sharedSecretService;
    }

    @Override
    public Plugin install(final String username, final Document document)
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
            log.error("An exception occurred while installing the plugin '[" + installedPlugin.getKey() + "]. Uninstalling...",e);
            pluginController.uninstall(installedPlugin);
            throw e;
        }
        
        return installedPlugin;
    }

    @Override
    public Plugin install(String username, String jsonDescriptor)
    {
        String pluginKey;
        try
        {
            ConnectAddonBean addOn = ConnectModulesGsonFactory.getGson(bundleContext).fromJson(jsonDescriptor, ConnectAddonBean.class);
            pluginKey = addOn.getKey();

            removeOldPlugin(addOn.getKey());
            final PluginArtifact pluginArtifact = remotePluginArtifactFactory.create(addOn, username);

            long startTime = System.currentTimeMillis();
            Plugin installedPlugin = installPlugin(pluginArtifact, pluginKey, username);

            try
            {
                AuthenticationType authType = addOn.getAuthentication().getType();
                final boolean useSharedSecret = addOnUsesSymmetricSharedSecret(authType); // TODO ACDEV-378: also check the algorithm
                String sharedSecret = useSharedSecret ? sharedSecretService.next() : null;
                String addOnSigningKey = useSharedSecret ? sharedSecret : addOn.getAuthentication().getPublicKey(); // the key stored on the applink: used to sign outgoing requests and verify incoming requests
                
                //applink MUST be created before any modules
                connectApplinkManager.createAppLink(installedPlugin, addOn.getBaseUrl(), authType, addOnSigningKey);

                //create the modules
                beanToModuleRegistrar.registerDescriptorsForBeans(installedPlugin, addOn);

                //save the descriptor so we can use it again if we ever need to re-enable the addon
                connectDescriptorRegistry.storeDescriptor(pluginKey,jsonDescriptor);
                
                //make the sync callback if needed
                connectEventHandler.pluginInstalled(addOn, sharedSecret);
                
                /*
                We need to manually fire the enabled event because the actual plugin enabled already fired and we ignored it.
                This is so we can register webhooks during the module registration phase and they will get fired with this enabled event.
                 */
                connectEventHandler.publishEnabledEvent(pluginKey);
                
            }
            catch (IllegalStateException e)
            {
                uninstallWithException(installedPlugin, e);
            }
            catch (Exception e)
            {
                uninstallWithException(installedPlugin, e);
            }

            long endTime = System.currentTimeMillis();

            log.info("Connect add-on installed in " + (endTime - startTime) + "ms");

            return installedPlugin;

        }
        catch (PluginInstallException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new InstallationFailedException(e.getCause() != null ? e.getCause() : e);
        }

    }

    private boolean addOnUsesSymmetricSharedSecret(AuthenticationType authType)
    {
        return AuthenticationType.JWT.equals(authType);
    }

    private void uninstallWithException(Plugin installedPlugin, Exception e) throws Exception
    {
        log.error("An exception occurred while installing the plugin '[" + installedPlugin.getKey() + "]. Uninstalling...",e);
        beanToModuleRegistrar.unregisterDescriptorsForPlugin(installedPlugin);
        pluginController.uninstall(installedPlugin);
        throw e;
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
