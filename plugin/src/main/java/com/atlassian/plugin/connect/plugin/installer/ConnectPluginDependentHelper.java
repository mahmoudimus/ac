package com.atlassian.plugin.connect.plugin.installer;

import java.lang.reflect.*;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.manager.DefaultPluginManager;
import com.atlassian.plugin.manager.PluginPersistentState;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is here to work around PLUGDEV-38. It provides methods to deal with dependent plugins outside of the normal plugin lifecycle.
 */
@Named
public class ConnectPluginDependentHelper
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginDependentHelper.class);
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final ConnectAddonRegistry connectRegistry;
    private final BundleContext bundleContext;

    @Inject
    public ConnectPluginDependentHelper(PluginAccessor pluginAccessor, PluginController pluginController, ConnectAddonRegistry connectRegistry, BundleContext bundleContext)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginController = pluginController;
        this.connectRegistry = connectRegistry;
        this.bundleContext = bundleContext;
    }

    /**
     * Returns whether the enabled state is persistent. e.g. should the plugin system enable this on a restart.
     * Note that this method just returns if the enabled state equals true in the database. If there's no value at
     * all for the plugin state in the db, the plugin system treats it as enabled=true, but this method will return false
     * meaning the state is not actually persisted even though plug core will assume enabled=true
     */
    public boolean isEnabledPersistent(Plugin plugin)
    {
        try
        {
            Map<String, Boolean> persistentPluginMap = getPersistentMap(plugin);
            if ((persistentPluginMap.containsKey(plugin.getKey()) && Boolean.TRUE.equals(persistentPluginMap.get(plugin.getKey()))))
            {
                return true;
            }

        }
        catch (Exception e)
        {
            log.error("Unable to get persistent state for plugin '" + plugin.getKey() + "' : " + e.getMessage(), e);
            //ignore the exception
            return false;
        }

        return false;
    }

    /**
     * Returns whether the disabled state is persistent. e.g. should the plugin system NOT enable this on a restart.
     * Note that this method just returns (true) if the enabled state equals false in the database. If there's no value at
     * all for the plugin state in the db, the plugin system treats it as enabled=true, and this method will return false
     * meaning the state is not actually persisted even though plug core will assume enabled=true
     */
    public boolean isDisabledPersistent(Plugin plugin)
    {
        try
        {
            Map<String, Boolean> persistentPluginMap = getPersistentMap(plugin);
            if ((persistentPluginMap.containsKey(plugin.getKey()) && Boolean.FALSE.equals(persistentPluginMap.get(plugin.getKey()))))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            log.error("Unable to get persistent state for plugin '" + plugin.getKey() + "' : " + e.getMessage(), e);

            //ignore the exception
            return false;
        }

        return false;
    }

    private Map<String, Boolean> getPersistentMap(Plugin plugin) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {
        ServiceReference ref = bundleContext.getServiceReference(PluginController.class.getName());
        Object serv = bundleContext.getService(ref);

        Proxy proxy = (Proxy) serv;
        InvocationHandler handler = Proxy.getInvocationHandler(proxy);
        Field f = handler.getClass().getDeclaredField("service");
        f.setAccessible(true);

        Object service = f.get(handler);

        Method getState = DefaultPluginManager.class.getDeclaredMethod("getState");
        getState.setAccessible(true);
        PluginPersistentState persistentState = (PluginPersistentState) getState.invoke(service);
        return persistentState.getPluginStateMap(plugin);
    }

    public void disableDependentPluginsWithoutPersistingState(Plugin plugin)
    {
        Set<Plugin> dependents = getDependentPluginsToReEnable(plugin);

        for (Plugin dependent : dependents)
        {
            pluginController.disablePluginWithoutPersisting(dependent.getKey());
        }
    }

    /**
     * This was essentially copied from plug-core DefaultPluginManager since it's not exposed publically
     */
    private Set<Plugin> getDependentPlugins(Plugin plugin, Iterable<Plugin> pluginsToCheck)
    {

        Collection<Plugin> allPlugins = pluginAccessor.getPlugins();
        final Multimap<String, Plugin> dependencies = ArrayListMultimap.create();
        for (Plugin p : pluginsToCheck)
        {
            for (String key : p.getRequiredPlugins())
            {
                dependencies.put(key, p);
            }
        }

        final Set<Plugin> dependentPlugins = Sets.newHashSet();

        final ArrayDeque<String> queue = new ArrayDeque<String>();
        queue.add(plugin.getKey());
        final Set<String> visited = Sets.newHashSet(plugin.getKey());

        while (!queue.isEmpty())
        {
            final String pluginKey = queue.removeFirst();
            for (Plugin dependentPlugin : dependencies.get(pluginKey))
            {
                final String dependentPluginKey = dependentPlugin.getKey();
                if (visited.add(dependentPluginKey))
                {
                    dependentPlugins.add(dependentPlugin);
                    queue.addLast(dependentPluginKey);
                }
            }
        }
        return dependentPlugins;
    }

    public void enableDependentPluginsIfNeeded(Plugin plugin)
    {
        for (Plugin dependent : getDependentPluginsToReEnable(plugin))
        {
            if (null != dependent)
            {
                pluginController.enablePlugin(dependent.getKey());
            }
        }
    }

    private Set<Plugin> getDependentPluginsToReEnable(Plugin plugin)
    {
        Set<Plugin> pluginsToReEnable = new HashSet<Plugin>();
        Set<Plugin> allDependents = getDependentPlugins(plugin, pluginAccessor.getPlugins());

        for (Plugin dependent : allDependents)
        {
            if (PluginState.ENABLED.equals(dependent.getPluginState()) || !isDisabledPersistent(dependent))
            {
                pluginsToReEnable.add(dependent);
            }
        }

        return pluginsToReEnable;
    }

    private Set<Plugin> getDisabledDependentPlugins(Plugin plugin)
    {
        return getDependentPlugins(plugin, getDisabledPlugins());
    }

    private Iterable<Plugin> getDisabledPlugins()
    {
        Collection<Plugin> allThePlugins = pluginAccessor.getPlugins();
        List<Plugin> disabledPlugins = new ArrayList<Plugin>();

        for (Plugin plugin : allThePlugins)
        {
            if (PluginState.DISABLED.equals(plugin.getPluginState()))
            {
                disabledPlugins.add(plugin);
            }
        }

        return disabledPlugins;
    }
}
