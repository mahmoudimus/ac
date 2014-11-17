package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
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

@Named
public class ConnectPluginProperEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginProperEventHandler.class);
    private final PluginEventManager pluginEventManager;
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonManager addonManager;


    @Inject
    public ConnectPluginProperEventHandler(PluginEventManager pluginEventManager, ConnectAddonRegistry addonRegistry, ConnectAddonManager addonManager)
    {
        this.pluginEventManager = pluginEventManager;
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent) throws IOException, ConnectAddOnUserInitException
    {
        if (isTheConnectPlugin(pluginEnabledEvent.getPlugin()))
        {
            //enable all the addons if needed
            for (String addonKey : addonRegistry.getAddonKeysToEnableOnRestart())
            {
                addonManager.enableConnectAddon(addonKey);
            }
        }
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent) throws IOException
    {
        if (isTheConnectPlugin(beforePluginDisabledEvent.getPlugin()))
        {
            for (String pluginKey : addonRegistry.getAllAddonKeys())
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
