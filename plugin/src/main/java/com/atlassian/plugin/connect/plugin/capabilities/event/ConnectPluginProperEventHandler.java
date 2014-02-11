package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.installer.ConnectPluginDependentHelper;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@Named
public class ConnectPluginProperEventHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginProperEventHandler.class);
    private final PluginEventManager pluginEventManager;
    private final PluginEventLogger pluginEventLogger;
    private final ConnectPluginDependentHelper dependentHelper;
    private final EventPublisher eventPublisher;
    private final ConnectMirrorPluginEventHandler mirrorPluginEventHandler;

    @Inject
    public ConnectPluginProperEventHandler(PluginEventManager pluginEventManager, PluginEventLogger pluginEventLogger, ConnectPluginDependentHelper dependentHelper, EventPublisher eventPublisher, ConnectMirrorPluginEventHandler mirrorPluginEventHandler)
    {
        this.pluginEventManager = pluginEventManager;
        this.pluginEventLogger = pluginEventLogger;
        this.dependentHelper = dependentHelper;
        this.eventPublisher = eventPublisher;
        this.mirrorPluginEventHandler = mirrorPluginEventHandler;
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void pluginEnabled(PluginEnabledEvent pluginEnabledEvent) throws IOException, ConnectAddOnUserInitException
    {
        if (isTheConnectPlugin(pluginEnabledEvent.getPlugin()))
        {
            pluginEventLogger.log(pluginEnabledEvent.getPlugin(), "PluginEnabledEvent");
            
            //PLUGDEV-38 - we need to force the mirror handler to know we're enabled!!!
            mirrorPluginEventHandler.pluginEnabled(pluginEnabledEvent);
            
            dependentHelper.enableDependentPluginsIfNeeded(pluginEnabledEvent.getPlugin());
        }
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent) throws IOException
    {
        if (isTheConnectPlugin(beforePluginDisabledEvent.getPlugin()))
        {
            pluginEventLogger.log(beforePluginDisabledEvent.getPlugin(), "BeforePluginDisabledEvent");
            
            dependentHelper.isDisabledPersistent(beforePluginDisabledEvent.getPlugin());

            //PLUGDEV-38 - we need to force the mirror handler to know we're disabling!!!
            mirrorPluginEventHandler.beforePluginDisabled(beforePluginDisabledEvent);
            
            dependentHelper.disableDependentPluginsWithoutPersistingState(beforePluginDisabledEvent.getPlugin());
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
