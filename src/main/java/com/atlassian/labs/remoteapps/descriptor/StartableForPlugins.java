package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.StartableRemoteModule;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.DisposableBean;

import java.util.Iterator;

/**
 *
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
        runnables.put(pluginKey, runnable);
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
            for (Iterator<Runnable> i = runnables.get(key).iterator(); i.hasNext(); )
            {
                Runnable runnable = i.next();
                runnable.run();
                i.remove();
            }
        }
    }

    @PluginEventListener
    public synchronized void onPluginEnabledEvent(PluginEnabledEvent event)
    {

        if (started)
        {
            runRunnablesForPlugin(event.getPlugin().getKey());
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
