package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.upm.spi.PluginControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ExportAsService(PluginControlHandler.class)
@Named
public class ConnectUPMControlHandler implements PluginControlHandler
{
    private static final Logger log = LoggerFactory.getLogger(ConnectUPMControlHandler.class);

    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddonToPluginFactory addonToPluginFactory;

    @Inject
    public ConnectUPMControlHandler(ConnectAddonManager connectAddonManager, ConnectAddonToPluginFactory addonToPluginFactory)
    {
        this.connectAddonManager = connectAddonManager;
        this.addonToPluginFactory = addonToPluginFactory;
    }

    @Override
    public boolean canControl(String pluginKey)
    {
        return connectAddonManager.hasDescriptor(pluginKey);
    }

    @Override
    public void enablePlugins(String... pluginKeys)
    {
        for (String key : pluginKeys)
        {
            enablePlugin(key);
        }
    }

    @Override
    public boolean isPluginEnabled(String pluginKey)
    {
        return connectAddonManager.isAddonEnabled(pluginKey);
    }

    @Override
    public void disablePlugin(String pluginKey)
    {
        try
        {
            connectAddonManager.disableConnectAddon(pluginKey);
        }
        catch (ConnectAddOnUserDisableException e)
        {
            log.error("Unable to disable connect addon fully...", e);
        }
    }

    @Override
    public Plugin getPlugin(String pluginKey)
    {
        Plugin plugin = null;
        
        ConnectAddonBean addon = connectAddonManager.getExistingAddon(pluginKey);
        
        if(null != addon)
        {
            PluginState state = (isPluginEnabled(pluginKey)) ? PluginState.ENABLED : PluginState.DISABLED;
            plugin = addonToPluginFactory.create(addon,state);
        }
        
        return plugin;
    }

    @Override
    public Collection<? extends Plugin> getPlugins()
    {
        List<Plugin> plugins = new ArrayList<Plugin>();
        
        for(String pluginKey : connectAddonManager.getAllAddonKeys())
        {
            Plugin plugin = getPlugin(pluginKey);
            
            if(null != plugin)
            {
                plugins.add(plugin);
            }
            else
            {
                log.debug("found addon key: " + pluginKey + " in registry, but descriptor does not exist!!");
            }
        }
        
        return plugins;
    }

    @Override
    public void uninstall(Plugin plugin) throws PluginException
    {
        try
        {
            connectAddonManager.uninstallConnectAddon(plugin.getKey());
        }
        catch (ConnectAddOnUserDisableException e)
        {
            log.error("Unable to uninstall connect addon fully...", e);
        }
    }

    @Override
    public PluginRestartState getPluginRestartState(String pluginKey)
    {
        return PluginRestartState.NONE;
    }

    private void enablePlugin(String pluginKey)
    {
        connectAddonManager.enableConnectAddon(pluginKey);
    }
}
