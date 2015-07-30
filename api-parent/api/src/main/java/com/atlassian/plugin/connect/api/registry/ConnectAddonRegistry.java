package com.atlassian.plugin.connect.api.registry;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.installer.AddonSettings;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;


/**
 * The ConnectAddonRegistry is used to store information about connect addons so they can be retrieved at runtime without having to inspect the plugin jar or constantly marshall the json descriptor
 * This is simply a place to centralize access to common bits of info used in various components. Things like the json descriptor, baseurl, etc should be stored here.
 * We should NOT go overboard here as many values can either be calculated or are not used often enough to store here.
 * We should only store values that we are going to need often across multiple components in here.
 */
public interface ConnectAddonRegistry
{
    void removeAll(String pluginKey);

    void storeAddonSettings(String pluginKey, AddonSettings settings);

    String getDescriptor(String pluginKey);

    boolean hasDescriptor(String pluginKey);

    String getBaseUrl(String pluginKey);

    boolean hasBaseUrl(String pluginKey);

    String getSecret(String pluginKey);

    String getUserKey(String pluginKey);

    Iterable<String> getAllAddonKeys();

    boolean hasAddons();

    Iterable<ConnectAddonBean> getAllAddonBeans();

    void storeRestartState(String pluginKey, PluginState state);

    PluginState getRestartState(String pluginKey);
    
    Iterable<String> getAddonKeysToEnableOnRestart();

    AddonSettings getAddonSettings(String pluginKey);

    boolean hasAddonWithKey(String pluginKey);

    Option<ConnectAddonBean> getAddonBean(String pluginKey);
}
