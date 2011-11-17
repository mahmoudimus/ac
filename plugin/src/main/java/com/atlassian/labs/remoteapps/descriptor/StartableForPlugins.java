package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.DisposableBean;

import java.util.Iterator;

/**
 * Handles executing runnables when the plugin has been loaded and the system is fully up
 */
public class StartableForPlugins implements LifecycleAware, DisposableBean
{
    private final Multimap<String,Runnable> runnables = ArrayListMultimap.create();
    private boolean started = false;

    private final PluginEventManager pluginEventManager;

    public StartableForPlugins(PluginEventManager pluginEventManager)
    {
        this.pluginEventManager = pluginEventManager;
        pluginEventManager.register(this);
    }

    public synchronized void register(String pluginKey, Runnable runnable)
    {
        if (started)
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
        runnables.clear();
    }

    private void runRunnablesForPlugin(String key)
    {
        if (runnables.containsKey(key))
        {
            for (Runnable runnable : runnables.get(key))
            {
                runnable.run();
            }
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
