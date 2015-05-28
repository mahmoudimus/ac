package com.atlassian.plugin.connect.core.integration.plugins;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.springframework.beans.factory.DisposableBean;

/**
 * Handles executing runnables when the plugin has been loaded and the system is fully up
 */
@ExportAsService(LifecycleAware.class)
@Named
public class StartableForPlugins implements LifecycleAware, DisposableBean
{
    private final Multimap<String,Runnable> runnables = ArrayListMultimap.create();
    private boolean started = false;

    private final PluginEventManager pluginEventManager;
    private final PluginAccessor pluginAccessor;

    @Inject
    public StartableForPlugins(PluginEventManager pluginEventManager, PluginAccessor pluginAccessor)
    {
        this.pluginEventManager = pluginEventManager;
        this.pluginAccessor = pluginAccessor;
        pluginEventManager.register(this);
    }

    public synchronized void register(String pluginKey, Runnable runnable)
    {
        if (pluginAccessor.isPluginEnabled(pluginKey))
        {
            runnable.run();
        }
        else
        {
            runnables.put(pluginKey, runnable);
        }
    }

    @Override
    public synchronized void onStart()
    {
        started = true;
        for (String key : runnables.keySet())
        {
            runRunnablesForPlugin(key);
        }
    }

    private void runRunnablesForPlugin(String key)
    {
        if (runnables.containsKey(key) && pluginAccessor.isPluginEnabled(key))
        {
            for (Runnable runnable : runnables.get(key))
            {
                runnable.run();
            }
            runnables.removeAll(key);
        }
    }

    @PluginEventListener
    public synchronized void onPluginEnabledEvent(PluginEnabledEvent event)
    {
        if (started)
        {
            runRunnablesForPlugin(event.getPlugin().getKey());
            runnables.removeAll(event.getPlugin().getKey());
        }
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(this);
    }

    public synchronized void unregister(String key)
    {
        runnables.removeAll(key);
    }
}
