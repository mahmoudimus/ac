package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.installer.*;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.atlassian.plugin.connect.spi.event.ConnectAddonEnabledEvent;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.*;
import com.atlassian.plugin.impl.AbstractPlugin;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.UriBuilder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData.newConnectAddonEventData;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

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
    private boolean connectPluginFullyEnabled;
    
    private final ConnectAddonManager connectAddonManager;
    private final ConnectPluginDependentHelper dependentHelper;
    private final JsonConnectAddOnIdentifierService connectIdentifier;
    private final PluginEventLogger pluginEventLogger;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final PluginEventManager pluginEventManager;

    @Inject
    public ConnectMirrorPluginEventHandler(ConnectAddonManager connectAddonManager, ConnectPluginDependentHelper dependentHelper, JsonConnectAddOnIdentifierService connectIdentifier, PluginEventLogger pluginEventLogger, RemotablePluginAccessorFactory remotablePluginAccessorFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, PluginEventManager pluginEventManager)
    {
        this.connectAddonManager = connectAddonManager;
        this.dependentHelper = dependentHelper;
        this.connectIdentifier = connectIdentifier;
        this.pluginEventLogger = pluginEventLogger;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.pluginEventManager = pluginEventManager;
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
            connectAddonManager.publishInstalledEvent(plugin,addon,sharedSecret);
        }
    }

    /**
     * This is called by the plugin system during installs and enables.
     * We may need to ignore this until connect specific stuff is setup.
     *
     * @param pluginEnabledEvent
     * @throws ConnectAddOnUserInitException
     */
    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent) throws ConnectAddOnUserInitException, IOException
    {
        final Plugin plugin = pluginEnabledEvent.getPlugin();

        if (isTheConnectPlugin(plugin))
        {
            this.connectPluginFullyEnabled = true;
            
            return;
        }

        /*
         Workaround for PLUGDEV-38. At this point the addon is marked as enabled, but we need it's state to be disabled so we
         get the enabled event again for the addon after connect is fully enabled
         */
        if (connectIdentifier.isConnectAddOn(plugin) && !connectPluginFullyEnabled && PluginState.ENABLED.equals(plugin.getPluginState()))
        {
            setPluginState(plugin, PluginState.DISABLED);
            return;
        }
        
        if(!connectPluginFullyEnabled)
        {
            return;
        }

        if (connectIdentifier.isConnectAddOn(plugin))
        {
            pluginEventLogger.log(pluginEnabledEvent.getPlugin(), "PluginEnabledEvent");
        }

        connectAddonManager.enableConnectAddon(plugin);
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void onPluginModuleEnabled(PluginModuleEnabledEvent event) throws IOException
    {
        if (!connectPluginFullyEnabled)
        {
            return;
        }

        if (connectIdentifier.isConnectAddOn(event.getModule().getPlugin()))
        {
            pluginEventLogger.log(event.getModule(), "PluginModuleEnabledEvent");
        }
        //Instances of remotablePluginAccessor are only meant to be used for the current operation and should not be cached across operations.
        remotablePluginAccessorFactory.remove(event.getModule().getPluginKey());
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent)
    {
        if (isTheConnectPlugin(beforePluginDisabledEvent.getPlugin()))
        {
            this.connectPluginFullyEnabled = false;
        }
    }
    
    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginDisabled(PluginDisabledEvent pluginDisabledEvent) throws ConnectAddOnUserDisableException, IOException
    {
        final Plugin plugin = pluginDisabledEvent.getPlugin();

        if (connectIdentifier.isConnectAddOn(plugin))
        {
            pluginEventLogger.log(pluginDisabledEvent.getPlugin(), "PluginDisabledEvent");
            
            connectAddonManager.disableConnectAddon(plugin);
        }

        // TODO remove this once we remove support for XML desciptors
        // ACDEV-886 -- unregister for ALL addons, as some XML descriptors register strategies
        iFrameRenderStrategyRegistry.unregisterAll(plugin.getKey());
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginUninstalled(PluginUninstalledEvent pluginUninstalledEvent) throws ConnectAddOnUserDisableException, IOException
    {
        final Plugin plugin = pluginUninstalledEvent.getPlugin();

        if (connectIdentifier.isConnectAddOn(plugin))
        {
            pluginEventLogger.log(plugin, "PluginUninstalledEvent");
        }
        
        connectAddonManager.uninstallConnectAddon(plugin);
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
     * @param pluginKey
     */
    public void publishEnabledEvent(String pluginKey)
    {
        connectAddonManager.publishEnabledEvent(pluginKey);
    }
}
