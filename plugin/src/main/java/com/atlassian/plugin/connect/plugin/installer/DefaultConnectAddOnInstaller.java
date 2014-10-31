package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkUtil;
import com.atlassian.plugin.connect.plugin.event.RemoteEventsHandler;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.event.ConnectAddonInstallFailedEvent;
import com.atlassian.plugin.connect.spi.event.RemotePluginInstallFailedEvent;
import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;

@Component
public class DefaultConnectAddOnInstaller implements ConnectAddOnInstaller
{
    @XmlDescriptor
    private final RemotePluginArtifactFactory remotePluginArtifactFactory;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;
    private final OAuthLinkManager oAuthLinkManager;
    private final RemoteEventsHandler remoteEventsHandler;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final ConnectAddonToPluginFactory addonToPluginFactory;
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddOnUserService connectAddOnUserService;

    private static final Logger log = LoggerFactory.getLogger(DefaultConnectAddOnInstaller.class);

    @Autowired
    public DefaultConnectAddOnInstaller(RemotePluginArtifactFactory remotePluginArtifactFactory,
            PluginController pluginController,
            PluginAccessor pluginAccessor,
            EventPublisher eventPublisher,
            OAuthLinkManager oAuthLinkManager,
            RemoteEventsHandler remoteEventsHandler,
            ConnectAddonBeanFactory connectAddonBeanFactory,
            ConnectAddonToPluginFactory addonToPluginFactory,
            ConnectAddonManager connectAddonManager,
            ConnectAddonRegistry addonRegistry,
            ConnectApplinkManager connectApplinkManager,
            ConnectAddOnUserService connectAddOnUserService)
    {
        this.remotePluginArtifactFactory = remotePluginArtifactFactory;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
        this.oAuthLinkManager = oAuthLinkManager;
        this.remoteEventsHandler = remoteEventsHandler;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
        this.addonToPluginFactory = addonToPluginFactory;
        this.connectAddonManager = connectAddonManager;
        this.addonRegistry = addonRegistry;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddOnUserService = connectAddOnUserService;
    }

    @Override
    @Deprecated
    @XmlDescriptor
    public Plugin install(final String username, final Document document) throws PluginInstallException
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        XmlDescriptorExploder.notifyAndExplode(pluginKey);

        removeOldPlugin(pluginKey);

        final PluginArtifact pluginArtifact = getPluginArtifact(username, document);

        Plugin installedPlugin = installXmlPlugin(pluginArtifact, pluginKey, username);

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
    public Plugin install(String jsonDescriptor) throws PluginInstallException
    {
        String pluginKey = null;
        Plugin addonPluginWrapper = null;
        ConnectAddonBean addOn;
        Option<ConnectAddonBean> maybePreviousAddon = Option.none();
        AddonSettings previousSettings = new AddonSettings();
        PluginState targetState = null;
        Option<ApplicationLink> maybePreviousApplink = Option.none();
        Option<AuthenticationType> maybePreviousAuthType = Option.none();
        Option<String> maybePreviousPublicKeyOrSharedSecret = Option.none();
        boolean reusePreviousPublicKeyOrSharedSecret = false;
        String baseUrl = "";

        long startTime = System.currentTimeMillis();

        try
        {
            //until we ensure we no longer have xml or mirror plugins, we need to call removeOldPlugin, which is why we marshal here just to get the plugin key
            ConnectAddonBean nonValidatedAddon = connectAddonBeanFactory.fromJsonSkipValidation(jsonDescriptor);

            pluginKey = nonValidatedAddon.getKey();
            maybePreviousApplink = Option.option(connectApplinkManager.getAppLink(pluginKey));
            maybePreviousAddon = findAddon(pluginKey);
            previousSettings = addonRegistry.getAddonSettings(pluginKey);
            targetState = PluginState.valueOf(previousSettings.getRestartState()); // don't go back to the registry unnecessarily; it will just return the same previousSettings

            if (maybePreviousApplink.isDefined() && maybePreviousAddon.isDefined())
            {
                ApplicationLink applink = maybePreviousApplink.get();
                baseUrl = applink.getRpcUrl().toString();
                maybePreviousAuthType = ConnectApplinkUtil.getAuthenticationType(applink);
                maybePreviousPublicKeyOrSharedSecret = connectApplinkManager.getSharedSecretOrPublicKey(applink);
                reusePreviousPublicKeyOrSharedSecret = true; // do NOT issue a new secret every time the add-on vendor updates their descriptor
            }
            else if (PluginState.UNINSTALLED.equals(targetState))
            {
                // has been installed and then uninstalled: we should sign the new installation with the old secret (if there was one)
                if (!StringUtils.isEmpty(previousSettings.getSecret()))
                {
                    maybePreviousPublicKeyOrSharedSecret = Option.some(previousSettings.getSecret());
                    // leave reusePreviousPublicKeyOrSharedSecret=false because we crossed an uninstall/reinstall boundary
                }

                targetState = PluginState.ENABLED; // we want the add-on to be usable by default after it is reinstalled
            }

            if (nonValidatedAddon.getModules().isEmpty())
            {
                com.atlassian.upm.api.util.Option<String> errorI18nKey = com.atlassian.upm.api.util.Option.<String>some("connect.install.error.no.modules");
                throw new PluginInstallException("Unable to install connect add on because it has no modules defined", errorI18nKey);
            }

            removeOldPlugin(pluginKey);

            addOn = connectAddonManager.installConnectAddon(jsonDescriptor, targetState, maybePreviousPublicKeyOrSharedSecret, reusePreviousPublicKeyOrSharedSecret);

            PluginState actualState = addonRegistry.getRestartState(pluginKey);
            addonPluginWrapper = addonToPluginFactory.create(addOn, actualState);
        }
        catch (Exception e)
        {
            if (null != pluginKey)
            {
                eventPublisher.publish(new ConnectAddonInstallFailedEvent(pluginKey, e.getMessage()));
                if (maybePreviousAddon.isDefined()
                    && maybePreviousApplink.isDefined()
                    && maybePreviousAuthType.isDefined())
                {
                    log.error("An exception occurred while installing the plugin '["
                              + pluginKey
                              + "]. Restoring previous version...", e);
                    ConnectAddonBean previousAddon = maybePreviousAddon.get();
                    String addonUserKey = this.connectAddOnUserService.getOrCreateUserKey(pluginKey,
                                                                                          previousAddon.getName());
                    addonRegistry.storeAddonSettings(pluginKey, previousSettings);
                    connectApplinkManager.createAppLink(previousAddon,
                                                        baseUrl,
                                                        maybePreviousAuthType.get(),
                                                        maybePreviousPublicKeyOrSharedSecret.getOrElse(""),
                                                        addonUserKey);
                    try
                    {
                        setAddonState(targetState, pluginKey);
                    }
                    catch (ConnectAddOnUserDisableException caude)
                    {
                        throw new PluginInstallException("Could not disable add", caude);
                    }
                    addonPluginWrapper = addonToPluginFactory.create(previousAddon);
                }
                else
                {
                    log.error("An exception occurred while installing the plugin '[" + pluginKey + "]. Uninstalling...",
                              e);
                    connectAddonManager.uninstallConnectAddonQuietly(pluginKey);

                    // if we were trying to reinstall after uninstalling then leave the previous "uninstalled" settings behind
                    // (i.e. nothing changed as a result of a failed re-installation attempt)
                    if (PluginState.UNINSTALLED.equals(PluginState.valueOf(previousSettings.getRestartState())))
                    {
                        log.error("An exception occurred while installing the plugin '["
                                + pluginKey
                                + "]. Restoring previous uninstalled-remnant settings...", e);
                        addonRegistry.storeAddonSettings(pluginKey, previousSettings);
                    }
                }
            }
            if(e instanceof PluginInstallException)
            {
                throw (PluginInstallException) e;
            }
            else
            {
                throw new PluginInstallException(e.getMessage(), e);
            }
        }

        long endTime = System.currentTimeMillis();

        log.info("Connect add-on installed in " + (endTime - startTime) + "ms");

        return addonPluginWrapper;
    }

    private Option<ConnectAddonBean> findAddon(final String pluginKey)
    {
        Iterable<ConnectAddonBean> allAddons = addonRegistry.getAllAddonBeans();
        return Iterables.findFirst(allAddons, new Predicate<ConnectAddonBean>()
        {
            @Override
            public boolean apply(@Nullable ConnectAddonBean input)
            {
                if (input == null)
                {
                    return false;
                }
                return pluginKey.equals(input.getKey());
            }
        });
    }

    private void setAddonState(PluginState targetState, String pluginKey) throws ConnectAddOnUserDisableException
    {
        if (null == targetState)
        {
            return;
        }
        else if (targetState == PluginState.ENABLED)
        {
            connectAddonManager.enableConnectAddon(pluginKey);
        }
        else if (targetState == PluginState.DISABLED)
        {
            connectAddonManager.disableConnectAddon(pluginKey);
        }
    }

    @XmlDescriptor
    @Deprecated
    private Plugin installXmlPlugin(PluginArtifact pluginArtifact, String pluginKey, String username)
    {
        XmlDescriptorExploder.notifyAndExplode(pluginKey);

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
                    new Object[] { pluginKey, username, ex.getMessage() });
            log.debug("Installation failed due to permission issue", ex);
            eventPublisher.publish(new RemotePluginInstallFailedEvent(pluginKey, "Installation failed due to permission issue " + ex.getMessage()));
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            log.warn("Unable to install remote plugin '{}' by user '{}' due to installation issue: {}",
                    new Object[] { pluginKey, username, ex.getMessage() });
            log.debug("Installation failed due to installation issue", ex);
            eventPublisher.publish(new RemotePluginInstallFailedEvent(pluginKey, ex.getMessage()));
            throw ex;
        }
        catch (PluginInstallException e)
        {
            eventPublisher.publish(new RemotePluginInstallFailedEvent(pluginKey, e.getMessage()));
            throw e;
        }
        catch (Exception e)
        {
            log.warn("Unable to install remote plugin '{}' by user '{}'", pluginKey, username);
            log.debug("Installation failed due to unknown issue", e);
            eventPublisher.publish(new RemotePluginInstallFailedEvent(pluginKey, e.getMessage()));
            throw new InstallationFailedException(e.getCause() != null ? e.getCause() : e);
        }
    }

    @Deprecated
    @XmlDescriptor
    private PluginArtifact getPluginArtifact(String username, Document document)
    {
        XmlDescriptorExploder.notifyAndExplode(null == document ? null : document.getRootElement().attributeValue("key"));

        if (document.getRootElement().attribute("plugins-version") != null)
        {
            return remotePluginArtifactFactory.create(document, username);
        }
        else
        {
            throw new InstallationFailedException("Missing plugins-version");
        }
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

            final ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);
            if (appLink != null)
            {
                // Blow away the applink
                oAuthLinkManager.unassociateProviderWithLink(appLink);
                connectApplinkManager.deleteAppLink(pluginKey);
            }
        }
        else if (connectAddonManager.hasDescriptor(pluginKey))
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
             If it was created by connect then it is ok
            */
            if (oAuthLinkManager.isAppAssociated(pluginKey))
            {
                final ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);
                if (appLink != null)
                {
                    // Is an applink created by connect.
                    // Blow away the applink
                    oAuthLinkManager.unassociateProviderWithLink(appLink);
                    connectApplinkManager.deleteAppLink(pluginKey);
                }
                else
                {
                    throw new PermissionDeniedException(pluginKey, "App key '" + pluginKey + "' is already associated with an OAuth link");
                }
            }
        }
    }
}
