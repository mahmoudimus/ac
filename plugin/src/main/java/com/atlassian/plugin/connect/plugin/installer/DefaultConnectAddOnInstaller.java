package com.atlassian.plugin.connect.plugin.installer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
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

import static com.google.common.collect.Lists.newArrayList;

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

    private static final Logger log = LoggerFactory.getLogger(DefaultConnectAddOnInstaller.class);

    @Autowired
    public DefaultConnectAddOnInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory, PluginController pluginController, PluginAccessor pluginAccessor, OAuthLinkManager oAuthLinkManager, RemoteEventsHandler remoteEventsHandler, BeanToModuleRegistrar beanToModuleRegistrar, BundleContext bundleContext)
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.remoteEventsHandler = remoteEventsHandler;
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.bundleContext = bundleContext;
    }

    @Override
    public Plugin install(final String username, final Document document)
    {
        String pluginKey = getPluginKey(document);
        removeOldPlugin(pluginKey);

        final PluginArtifact pluginArtifact = getPluginArtifact(username, document);

        return installPlugin(pluginArtifact, pluginKey, username);
    }

    @Override
    public Plugin install(String username, String capabilities)
    {
        String pluginKey = "unknown";
        try
        {
            ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson(bundleContext).fromJson(capabilities, ConnectAddonBean.class);
            pluginKey = addOn.getKey();

            removeOldPlugin(addOn.getKey());
            final PluginArtifact pluginArtifact = remotePluginArtifactFactory.create(addOn, username);

            long startTime = System.currentTimeMillis();
            Plugin installedPlugin = installPlugin(pluginArtifact, pluginKey, username);

            //we need to make sure the container is registered first
            List<CapabilityBean> capabilityBeans = newArrayList();
            if (addOn.getCapabilities().containsKey("connect-container"))
            {
                RemoteContainerCapabilityBean container = ((List<RemoteContainerCapabilityBean>) addOn.getCapabilities().get("connect-container")).get(0);
                if (null == container)
                {
                    throw new InstallationFailedException("No connect-container found in capabilities!");
                }
                
                capabilityBeans.add(container);
            }
            
            //we need to register the container first
            
            try
            {

                for (Map.Entry<String, List<? extends CapabilityBean>> entry : addOn.getCapabilities().entrySet())
                {
                    if(!"connect-container".equals(entry.getKey()))
                    {
                        capabilityBeans.addAll(entry.getValue());
                    }
                }

                beanToModuleRegistrar.registerDescriptorsForBeans(installedPlugin, capabilityBeans);
            }
            catch (Exception e)
            {
                beanToModuleRegistrar.unregisterDescriptorsForPlugin(installedPlugin);
                pluginController.uninstall(installedPlugin);
                throw e;
            }

            long endTime = System.currentTimeMillis();

            log.info("Capabilities based connect app started in " + (endTime - startTime) + "ms");

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

    private Plugin installPlugin(PluginArtifact pluginArtifact, String pluginKey, String username)
    {
        Plugin installedPlugin = null;
        int moduleSize = 0;
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

            try
            {
                remoteEventsHandler.pluginInstalled(pluginKey);
            }
            catch (PluginInstallException e)
            {
                pluginController.uninstall(installedPlugin);
                throw e;
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
