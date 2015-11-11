package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.plugin.AddonSettings;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.PermissionDeniedException;
import com.atlassian.plugin.connect.plugin.auth.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.auth.applinks.ConnectApplinkUtil;
import com.atlassian.plugin.connect.plugin.auth.oauth.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.descriptor.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.lifecycle.event.ConnectAddonInstallFailedEvent;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.ConnectAddonToPluginFactory;
import com.atlassian.plugin.connect.spi.auth.user.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExportAsService(ConnectAddOnInstaller.class)
public class DefaultConnectAddOnInstaller implements ConnectAddOnInstaller
{
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;
    private final OAuthLinkManager oAuthLinkManager;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final ConnectAddonToPluginFactory addonToPluginFactory;
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectUserService connectUserService;

    private static final Logger log = LoggerFactory.getLogger(DefaultConnectAddOnInstaller.class);

    @Autowired
    public DefaultConnectAddOnInstaller(PluginController pluginController,
            PluginAccessor pluginAccessor,
            EventPublisher eventPublisher,
            OAuthLinkManager oAuthLinkManager,
            ConnectAddonBeanFactory connectAddonBeanFactory,
            ConnectAddonToPluginFactory addonToPluginFactory,
            ConnectAddonManager connectAddonManager,
            ConnectAddonRegistry addonRegistry,
            ConnectApplinkManager connectApplinkManager,
            ConnectUserService connectUserService)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
        this.oAuthLinkManager = oAuthLinkManager;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
        this.addonToPluginFactory = addonToPluginFactory;
        this.connectAddonManager = connectAddonManager;
        this.addonRegistry = addonRegistry;
        this.connectApplinkManager = connectApplinkManager;
        this.connectUserService = connectUserService;
    }

    @Override
    public Plugin install(String jsonDescriptor) throws ConnectAddOnInstallException
    {
        String pluginKey = null;
        Plugin addonPluginWrapper;
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
            ConnectAddonBean addOn = connectAddonBeanFactory.fromJson(jsonDescriptor);
            validateModules(addOn);

            pluginKey = addOn.getKey();
            maybePreviousApplink = Option.option(connectApplinkManager.getAppLink(pluginKey));
            previousSettings = addonRegistry.getAddonSettings(pluginKey);
            targetState = PluginState.valueOf(previousSettings.getRestartState());

            if (maybePreviousApplink.isDefined() && !Strings.isNullOrEmpty(previousSettings.getDescriptor()))
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

            removeOldPlugin(pluginKey);

            connectAddonManager.installConnectAddon(jsonDescriptor, targetState, maybePreviousPublicKeyOrSharedSecret, reusePreviousPublicKeyOrSharedSecret);

            PluginState actualState = addonRegistry.getRestartState(pluginKey);
            addonPluginWrapper = addonToPluginFactory.create(addOn, actualState);
        }
        catch (Exception e)
        {
            if (null != pluginKey)
            {
                eventPublisher.publish(new ConnectAddonInstallFailedEvent(pluginKey, e.getMessage()));
                if (!Strings.isNullOrEmpty(previousSettings.getDescriptor())
                    && maybePreviousApplink.isDefined()
                    && maybePreviousAuthType.isDefined())
                {
                    log.error("An exception occurred while installing the plugin '["
                              + pluginKey
                              + "]. Restoring previous version...", e);
                    ConnectAddonBean previousAddon = connectAddonBeanFactory.fromJson(previousSettings.getDescriptor());
                    String addonUserKey = this.connectUserService.getOrCreateAddOnUserName(pluginKey,
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
                        throw new ConnectAddOnInstallException("Could not disable add", caude);
                    }
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
            Throwables.propagateIfInstanceOf(e, ConnectAddOnInstallException.class);
            throw new ConnectAddOnInstallException(e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis();

        log.info("Connect add-on installed in " + (endTime - startTime) + "ms");

        return addonPluginWrapper;
    }

    private void validateModules(ConnectAddonBean addon) throws InvalidDescriptorException
    {
        try
        {
            addon.getModules().getModuleLists();
        }
        catch (ConnectModuleValidationException e)
        {
            InvalidDescriptorException exception = new InvalidDescriptorException(e.getMessage(),
                    e.getI18nKey(), e.getI18nParameters());
            exception.initCause(e);
            throw exception;
        }
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
