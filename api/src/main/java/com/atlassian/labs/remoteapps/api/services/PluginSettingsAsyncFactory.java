package com.atlassian.labs.remoteapps.api.services;

/**
 * Factory for mutable, non-threadsafe PluginSettingsAsync objects.
 *
 * @since 2.0
 */
public interface PluginSettingsAsyncFactory
{
    /**
     * Gets all settings for a key, for which valid values are application-specific (Confluence maps this to space keys,
     * JIRA to project keys, and FishEye to repository keys, for example). To store settings for other keys,
     * createGlobalSettings should be used, and the keys should be sensibly namespaced by the plugin.
     *
     * @param key the key, can be null to retrieve global settings
     * @throws IllegalArgumentException if no "concept" for the key can be found
     * @return The settings
     */
    PluginSettingsAsync createSettingsForKey(String key);

    /**
     * Gets all global settings. This is useful to store settings against arbitrary keys. When storing settings against
     * arbitrary keys, plugins are advised to namespace the key with something unique to the plugin (for example
     * "com.example.plugin:key-I-would-like-to-use" ) to avoid clashes with other keys.
     *
     * @return Global settings
     */
    PluginSettingsAsync createGlobalSettings();
}
