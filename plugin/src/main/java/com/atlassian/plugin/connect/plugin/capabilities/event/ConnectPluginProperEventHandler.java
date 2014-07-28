package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.installer.AddonSettings;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.registry.LegacyConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.usernameForAddon;

@Named
public class ConnectPluginProperEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginProperEventHandler.class);
    private final PluginEventManager pluginEventManager;
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonManager addonManager;
    
    //TODO: remove all deps below after initial deploy of file-less addons
    private final LegacyConnectAddonRegistry legacyRegistry;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final JsonConnectAddOnIdentifierService addOnIdentifierService;

    @Inject
    public ConnectPluginProperEventHandler(PluginEventManager pluginEventManager, ConnectAddonRegistry addonRegistry, ConnectAddonManager addonManager, LegacyConnectAddonRegistry legacyRegistry, PluginController pluginController, PluginAccessor pluginAccessor, JsonConnectAddOnIdentifierService addOnIdentifierService)
    {
        this.pluginEventManager = pluginEventManager;
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
        this.legacyRegistry = legacyRegistry;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.addOnIdentifierService = addOnIdentifierService;
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent) throws IOException, ConnectAddOnUserInitException
    {
        if (isTheConnectPlugin(pluginEnabledEvent.getPlugin()))
        {
            //enable all the addons if needed
            for(String addonKey : addonRegistry.getAddonKeysToEnableOnRestart())
            {
                addonManager.enableConnectAddon(addonKey);
            }
        }
        
        //TODO: remove this call after the initial deploy of file-less addons
        convertOldAddons();
    }

    //TODO: remove this method after the initial deploy of file-less addons
    private void convertOldAddons()
    {
        for(Plugin plugin : pluginAccessor.getPlugins())
        {
            try
            {
                convertOldAddon(plugin);
            }
            catch (Exception e)
            {
                log.error("Failed to convert P2 addon to file-less for key {}", plugin.getKey());
            }
        }
    }

    private void convertOldAddon(Plugin plugin)
    {
        if (addOnIdentifierService.isConnectAddOn(plugin))
        {
            log.info("Converting old P2 addon to new file-less addon: " + plugin.getKey());
            String pluginKey = plugin.getKey();
            String restartState = (PluginState.ENABLED.equals(plugin.getPluginState())) ? PluginState.ENABLED.name() : PluginState.DISABLED.name();

            String descriptor = legacyRegistry.getDescriptor(pluginKey);
            ConnectAddonBean connectAddonBean = ConnectModulesGsonFactory.addonFromJsonWithI18nCollector(descriptor, null);

            AddonSettings settings = new AddonSettings()
                    .setDescriptor(descriptor)
                    .setRestartState(restartState)
                    .setUserKey(legacyRegistry.hasUserKey(pluginKey) ?
                            legacyRegistry.getUserKey(pluginKey) :
                            usernameForAddon(pluginKey))
                    .setAuth(connectAddonBean.getAuthentication().getType().name())
                    .setBaseUrl(connectAddonBean.getBaseUrl())
                    .setSecret(legacyRegistry.getSecret(pluginKey));

            addonRegistry.storeAddonSettings(pluginKey, settings);

            if (PluginState.ENABLED.equals(plugin.getPluginState())) {
                addonManager.enableConnectAddon(pluginKey);
            }

            pluginController.uninstall(plugin);
        }
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent) throws IOException
    {
        if (isTheConnectPlugin(beforePluginDisabledEvent.getPlugin()))
        {
            for(String pluginKey : addonRegistry.getAllAddonKeys())
            {
                try
                {
                    addonManager.disableConnectAddonWithoutPersistingState(pluginKey);
                }
                catch (ConnectAddOnUserDisableException e)
                {
                    log.error("Unable to disable addon user for addon: " + pluginKey, e);
                }
            }
        }
    }

    private boolean isTheConnectPlugin(Plugin plugin)
    {
        return (ConnectPluginInfo.getPluginKey().equals(plugin.getKey()));
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        this.pluginEventManager.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        this.pluginEventManager.unregister(this);
    }

}
