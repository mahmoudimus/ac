package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.installer.*;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.*;
import com.atlassian.plugin.impl.AbstractPlugin;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This is the central place to handle PluginEvents broadcasted from plug-core as they relate to connect addons.
 * Any PluginEvent should be handled here. This is essentially the implementation of the addon lifecycle.
 */
@Named
public class ConnectMirrorPluginEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectMirrorPluginEventHandler.class);

    /*
    This is a workaround for PLUGDEV-38. e.g. we must make sure the connect plugin is enabled before we do anything with dependent plugins/addons
     */
    private final AtomicBoolean connectPluginFullyEnabled;

    private final ConnectAddonManager connectAddonManager;
    private final ConnectPluginDependentHelper dependentHelper;
    private final JsonConnectAddOnIdentifierService connectIdentifier;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final PluginEventManager pluginEventManager;
    private final ConnectAddonRegistry descriptorRegistry;

    @Inject
    public ConnectMirrorPluginEventHandler(ConnectAddonManager connectAddonManager, ConnectPluginDependentHelper dependentHelper, JsonConnectAddOnIdentifierService connectIdentifier, RemotablePluginAccessorFactory remotablePluginAccessorFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, PluginEventManager pluginEventManager, ConnectAddonRegistry descriptorRegistry)
    {
        this.connectAddonManager = connectAddonManager;
        this.dependentHelper = dependentHelper;
        this.connectIdentifier = connectIdentifier;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.pluginEventManager = pluginEventManager;
        this.descriptorRegistry = descriptorRegistry;
        this.connectPluginFullyEnabled = new AtomicBoolean(false);
    }

    /**
     * Called directly from the {@link com.atlassian.plugin.connect.plugin.installer.ConnectUPMInstallHandler} when a plugin is installed.
     * This needs to be called manually instead of listening for a PluginInstalledEvent as we do special handling
     *
     * @param plugin       The plugin that was installed
     * @param addon        The addon bean we're installing
     * @param sharedSecret The addon's shared secret if it is JWT
     */
    public void pluginInstalled(Plugin plugin, ConnectAddonBean addon, String sharedSecret)
    {
        if (!Strings.isNullOrEmpty(addon.getLifecycle().getInstalled()))
        {
            //connectAddonManager.publishInstalledEvent(plugin, addon, sharedSecret);
        }
    }

    /**
     * This is called by the plugin system during installs and enables.
     * We may need to ignore this until connect specific stuff is setup.
     *
     * @param pluginEnabledEvent
     * @throws com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException
     */
    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent) throws ConnectAddOnUserInitException, IOException
    {
//        final Plugin plugin = pluginEnabledEvent.getPlugin();
//
//        if (isTheConnectPlugin(plugin))
//        {
//            
//            this.connectPluginFullyEnabled.set(true);
//            return;
//        }
//
//        /*
//         Workaround for PLUGDEV-38. At this point the addon is marked as enabled, but we need it's state to be disabled so we
//         get the enabled event again for the addon after connect is fully enabled
//         */
//        if (connectIdentifier.isConnectAddOn(plugin) && !connectPluginFullyEnabled.get() && PluginState.ENABLED.equals(plugin.getPluginState()))
//        {
//            setPluginState(plugin, PluginState.DISABLED);
//            return;
//        }
//
//        if (!connectPluginFullyEnabled.get())
//        {
//            return;
//        }
//
//        connectAddonManager.enableConnectAddon(plugin);
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void onPluginModuleEnabled(PluginModuleEnabledEvent event) throws IOException
    {
//        if (!connectPluginFullyEnabled.get())
//        {
//            return;
//        }
//
//        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
//        remotablePluginAccessorFactory.remove(event.getModule().getPluginKey());
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent)
    {
//        final Plugin plugin = beforePluginDisabledEvent.getPlugin();
//
//        if (isTheConnectPlugin(plugin))
//        {
//            this.connectPluginFullyEnabled.set(false);
//        }
//
//        if (connectIdentifier.isConnectAddOn(plugin))
//        {
//            //we need to publish the disabled event to the remote addon BEFORE we actually do the disable
//            // so that the webhook modules that actually make the call are still available
//            connectAddonManager.publishDisabledEvent(plugin.getKey());
//        }
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginDisabled(PluginDisabledEvent pluginDisabledEvent) throws ConnectAddOnUserDisableException, IOException
    {
//        final Plugin plugin = pluginDisabledEvent.getPlugin();
//
//        if (connectIdentifier.isConnectAddOn(plugin))
//        {
//            connectAddonManager.disableConnectAddon(plugin);
//        }
//
//        // TODO remove this once we remove support for XML desciptors
//        // ACDEV-886 -- unregister for ALL addons, as some XML descriptors register strategies
//        iFrameRenderStrategyRegistry.unregisterAll(plugin.getKey());
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginUninstalled(PluginUninstalledEvent pluginUninstalledEvent) throws ConnectAddOnUserDisableException, IOException
    {
//        final Plugin plugin = pluginUninstalledEvent.getPlugin();
//
//        connectAddonManager.uninstallConnectAddon(plugin);
    }

    private void setPluginState(Plugin plugin, PluginState state)
    {
        try
        {
            Method setPluginStateMethod = AbstractPlugin.class.getDeclaredMethod("setPluginState", PluginState.class);
            setPluginStateMethod.setAccessible(true);
            setPluginStateMethod.invoke(plugin, state);

        }
        catch (Exception e)
        {
            log.error("Unable to reflectively set pluginState to " + state.name() + " for plugin '" + plugin.getKey(), e);
        }
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

    private boolean isTheConnectPlugin(Plugin plugin)
    {
        return (ConnectPluginInfo.getPluginKey().equals(plugin.getKey()));
    }

    /**
     * Just here to delegate so others that get us injected don't have to also get the connectAddonManager injected just for this call
     *
     * @param pluginKey
     */
    public void publishEnabledEvent(String pluginKey)
    {
        connectAddonManager.publishEnabledEvent(pluginKey);
    }
}
