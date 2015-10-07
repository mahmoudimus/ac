package com.atlassian.plugin.connect.jira;

import com.atlassian.plugin.connect.jira.integration.plugins.ConnectAddonResourceBundle;
import com.atlassian.plugin.connect.spi.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.google.common.collect.Maps.newHashMap;

/**
 * This class loads a plugin called "atlassian-connect.i18n", which solely exists to store and expose generated i18n files
 * from remote descriptors.  Ideally, the products would support generated i18n resources, but currently they expect
 * them to be declared via {@code <resource>} tags pointing at physical files in the plugin.  This plugin is a way to get
 * around that.
 */
@JiraComponent
public class JiraAddonI18nManager implements ConnectAddonI18nManager
{

    /**
     * Register i18n properties for a newly installed plugin.
     */
    @Override
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
    @Override
    public synchronized void add(String addonKey, Map<String, String> i18nMap) throws IOException
    {
        ConnectAddonResourceBundle bundle = (ConnectAddonResourceBundle) ResourceBundle.getBundle(ConnectAddonResourceBundle.class.getName());

        bundle.add(i18nMap);
    }
}
