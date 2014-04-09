package com.atlassian.plugin.connect.plugin.integration.plugins;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Maps.newHashMap;

/**
 * This class loads a plugin called "atlassian-connect.i18n", which solely exists to store and expose generated i18n files
 * from remote descriptors.  Ideally, the products would support generated i18n resources, but currently they expect
 * them to be declared via <resource> tags pointing at physical files in the plugin.  This plugin is a way to get
 * around that.
 */
@Component
public class I18nPropertiesPluginManager
{
    private static final Logger log = LoggerFactory.getLogger(I18nPropertiesPluginManager.class);

    private final Plugin theConnectPlugin;

    @Autowired
    public I18nPropertiesPluginManager(PluginRetrievalService pluginRetrievalService)
    {
        this.theConnectPlugin = pluginRetrievalService.getPlugin();
    }

    /**
     * Register i18n properties for a newly installed plugin.
     */
    public synchronized void add(String addonKey, Properties i18nProperties) throws IOException
    {
        Map<String, String> map = newHashMap();
        for (final String name : i18nProperties.stringPropertyNames())
        {
            map.put(name, i18nProperties.getProperty(name));
        }

        add(addonKey, map);
    }

    /**
     * Register i18n properties for a newly installed plugin.
     */
    public synchronized void add(String addonKey, Map<String, String> i18nMap) throws IOException
    {
        ConnectAddonResourceBundle bundle = (ConnectAddonResourceBundle) ResourceBundle.getBundle(ConnectAddonResourceBundle.class.getName());

        bundle.add(i18nMap);
    }

}
