package com.atlassian.plugin.connect.plugin.registry;

import java.util.List;
import java.util.Set;

import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.installer.AddonSettings;

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
    
    void storeDescriptor(String pluginKey, String json);

    void removeDescriptor(String pluginKey);

    String getDescriptor(String pluginKey);

    boolean hasDescriptor(String pluginKey);

    void storeBaseUrl(String pluginKey, String url);

    void removeBaseUrl(String pluginKey);

    String getBaseUrl(String pluginKey);

    boolean hasBaseUrl(String pluginKey);

    void storeSecret(String pluginKey, String secret);

    void removeSecret(String pluginKey);

    String getSecret(String pluginKey);

    boolean hasSecret(String pluginKey);

    void storeUserKey(String pluginKey, String userKey);

    void removeUserKey(String pluginKey);

    String getUserKey(String pluginKey);

    boolean hasUserKey(String pluginKey);

    void storeAuthType(String pluginKey, AuthenticationType type);

    void removeAuthType(String pluginKey);

    AuthenticationType getAuthType(String pluginKey);

    boolean hasAuthType(String pluginKey);
    
    Iterable<String> getAllAddonKeys();

    void storeRestartState(String pluginKey, PluginState state);

    void removeRestartState(String pluginKey);

    PluginState getRestartState(String pluginKey);
    
    Iterable<String> getAddonKeysToEnableOnRestart();
}
