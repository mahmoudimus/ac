package com.atlassian.plugin.remotable.container.service.sal;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.core.message.AbstractI18nResolver;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

/**
 * Returns the key with args as a string
 */
public class ContainerI18nResolver extends AbstractI18nResolver
{
    private ManagedLock.ReadWrite locks = ManagedLocks.manageReadWrite(new ReentrantReadWriteLock());
    private final Map<Plugin, Iterable<String>> pluginResourceBundleNames = new ConcurrentHashMap<Plugin, Iterable<String>>();

    private final ResourceBundleResolver resolver;

    public ContainerI18nResolver(final PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager,
            ResourceBundleResolver resolver)
    {
        pluginEventManager.register(this);
        this.resolver = assertNotNull(resolver, "resolver");

        locks.write().withLock(new Runnable()
        {
            public void run()
            {
                addPluginResourceBundles(pluginAccessor.getPlugins());
            }
        });
    }

    public String getRawText(String key)
    {
        String pattern = getPattern(key);
        if (pattern == null)
        {
            pattern = key;
        }
        return pattern;
    }

    public String resolveText(String key, Serializable[] arguments)
    {
        String pattern = getPattern(key);
        if (pattern == null)
        {
            return key;
        }
        return MessageFormat.format(pattern, (Object[]) arguments);
    }

    private String getPattern(final String key)
    {
        return locks.read().withLock(new Supplier<String>()
        {
            public String get()
            {
                String bundleString = null;
                for (Entry<Plugin, Iterable<String>> pluginBundleNames : pluginResourceBundleNames.entrySet())
                {
                    for (String bundleName : pluginBundleNames.getValue())
                    {
                        try
                        {
                            ResourceBundle bundle = getBundle(bundleName, Locale.getDefault(), pluginBundleNames.getKey());
                            bundleString = bundle.getString(key);
                        }
                        catch (MissingResourceException e)
                        {
                            // ignore, try next bundle
                        }
                    }
                }
                return bundleString;
            }
        });
    }

    public Map<String, String> getAllTranslationsForPrefix(final String prefix)
    {
        assertNotNull(prefix, "prefix");

        return locks.read().withLock(new Supplier<Map<String, String>>()
        {
            public Map<String, String> get()
            {
                Map<String, String> translationsWithPrefix = new HashMap<String, String>();
                for (Entry<Plugin, Iterable<String>> pluginBundleNames : pluginResourceBundleNames.entrySet())
                {
        
                    addMatchingTranslationsToMap(
                            prefix, Locale.getDefault(), pluginBundleNames.getKey(), pluginBundleNames.getValue(), translationsWithPrefix);
                }
                return translationsWithPrefix;
            }
        });
    }

    public Map<String, String> getAllTranslationsForPrefix(final String prefix, final Locale locale)
    {
        assertNotNull(prefix, "prefix");
        assertNotNull(locale, "locale");

        return locks.read().withLock(new Supplier<Map<String, String>>()
        {
            public Map<String, String> get()
            {
                Map<String, String> translationsWithPrefix = new HashMap<String, String>();
                for (Entry<Plugin, Iterable<String>> pluginBundleNames : pluginResourceBundleNames.entrySet())
                {
                    addMatchingTranslationsToMap(
                            prefix, locale, pluginBundleNames.getKey(), pluginBundleNames.getValue(), translationsWithPrefix);
                }
                return translationsWithPrefix;
            }
        });
    }

    private void addMatchingTranslationsToMap(String prefix, Locale locale, Plugin plugin,
                                              Iterable<String> bundleNames,
                                              Map<String, String> translationsWithPrefix)
    {
        for (String bundleName : bundleNames)
        {
            try
            {
                ResourceBundle bundle = getBundle(bundleName, locale, plugin);
                if (bundle != null)
                {
                    addMatchingTranslationsToMap(prefix, bundle, translationsWithPrefix);
                }
            }
            catch (MissingResourceException e)
            {
                // OK, just ignore
            }
        }
    }

    private void addMatchingTranslationsToMap(String prefix, ResourceBundle bundle,
                                              Map<String, String> translationsWithPrefix)
    {
        Enumeration enumeration = bundle.getKeys();
        while (enumeration.hasMoreElements())
        {
            String key = (String) enumeration.nextElement();
            if (key.startsWith(prefix))
            {
                translationsWithPrefix.put(key, bundle.getString(key));
            }
        }
    }

    @PluginEventListener
    public void pluginEnabled(final PluginEnabledEvent event)
    {
        locks.write().withLock(new Runnable()
        {
            public void run()
            {
                addPluginResourceBundles(event.getPlugin());
            }
        });
    }

    @PluginEventListener
    public void pluginDisabled(final PluginDisabledEvent event)
    {
        locks.write().withLock(new Runnable()
        {
            public void run()
            {
                removePluginResourceBundles(event.getPlugin());
            }
        });
    }

    private void addPluginResourceBundles(Iterable<Plugin> plugins)
    {
        for (Plugin plugin : plugins)
        {
            addPluginResourceBundles(plugin);
        }
    }

    private void addPluginResourceBundles(Plugin plugin)
    {
        List<String> bundleNames = new LinkedList<String>();
        Iterable<ResourceDescriptor> descriptors = plugin.getResourceDescriptors("i18n");
        for (ResourceDescriptor descriptor : descriptors)
        {
            bundleNames.add(descriptor.getLocation());
        }
        addPluginResourceBundles(plugin, bundleNames);
    }

    private void addPluginResourceBundles(Plugin plugin, List<String> bundleNames)
    {
        pluginResourceBundleNames.put(plugin, bundleNames);
    }

    private void removePluginResourceBundles(Plugin plugin)
    {
        pluginResourceBundleNames.remove(plugin);
    }

    private ResourceBundle getBundle(String bundleName, Locale locale, Plugin plugin)
    {
        return resolver.getBundle(bundleName, locale, plugin.getClassLoader());
    }

    private static <T> T assertNotNull(T object, String name)
    {
        if (object == null)
        {
            throw new NullPointerException(name + " must not be null");
        }
        return object;
    }
}
