package com.atlassian.plugin.connect.plugin.lifecycle.upm;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonEnableException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectAddonManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.upm.spi.PluginControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ExportAsService(PluginControlHandler.class)
@Named
public class ConnectUPMControlHandler implements PluginControlHandler {
    private static final Logger log = LoggerFactory.getLogger(ConnectUPMControlHandler.class);

    private ConnectAddonAccessor addonAccessor;
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddonToPluginFactory addonToPluginFactory;

    @Inject
    public ConnectUPMControlHandler(ConnectAddonAccessor addonAccessor,
                                    ConnectAddonManager connectAddonManager,
                                    ConnectAddonToPluginFactory addonToPluginFactory) {
        this.addonAccessor = addonAccessor;
        this.connectAddonManager = connectAddonManager;
        this.addonToPluginFactory = addonToPluginFactory;
    }

    @Override
    public boolean canControl(String pluginKey) {
        return connectAddonManager.hasDescriptor(pluginKey);
    }

    @Override
    public void enablePlugins(String... pluginKeys) {
        for (String key : pluginKeys) {
            try {
                connectAddonManager.enableConnectAddon(key);
            } catch (ConnectAddonEnableException e) {
                log.error("Tried to enable Connect add-on " + e.getAddonKey() + " from UPM, but couldn't: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isPluginEnabled(String pluginKey) {
        return addonAccessor.isAddonEnabled(pluginKey);
    }

    @Override
    public void disablePlugin(String pluginKey) {
        try {
            connectAddonManager.disableConnectAddon(pluginKey);
        } catch (ConnectAddonDisableException e) {
            log.error("Unable to disable connect addon fully...", e);
        }
    }

    @Override
    public Plugin getPlugin(String pluginKey) {
        try {
            return getPluginImpl(pluginKey);
        } catch (InvalidDescriptorException e) {
            PluginState state = getPluginStateForAddon(pluginKey);
            return addonToPluginFactory.create(pluginKey, state);
        }
    }

    private Plugin getPluginImpl(String pluginKey) {
        Plugin plugin = null;
        Optional<ConnectAddonBean> optionalAddon = addonAccessor.getAddon(pluginKey);
        if (optionalAddon.isPresent()) {
            PluginState state = getPluginStateForAddon(pluginKey);
            plugin = addonToPluginFactory.create(optionalAddon.get(), state);
        }
        return plugin;
    }

    private PluginState getPluginStateForAddon(String pluginKey) {
        return isPluginEnabled(pluginKey) ? PluginState.ENABLED : PluginState.DISABLED;
    }

    @Override
    public Collection<? extends Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<>();

        for (String pluginKey : connectAddonManager.getAllAddonKeys()) {
            Plugin plugin = getPlugin(pluginKey);

            if (null != plugin) {
                plugins.add(plugin);
            } else {
                log.debug("found addon key: " + pluginKey + " in registry, but descriptor does not exist!!");
            }
        }

        return plugins;
    }

    @Override
    public void uninstall(Plugin plugin) throws PluginException {
        try {
            connectAddonManager.uninstallConnectAddon(plugin.getKey());
        } catch (ConnectAddonDisableException e) {
            log.error("Unable to uninstall connect addon fully...", e);
        }
    }

    @Override
    public PluginRestartState getPluginRestartState(String pluginKey) {
        return PluginRestartState.NONE;
    }

}
