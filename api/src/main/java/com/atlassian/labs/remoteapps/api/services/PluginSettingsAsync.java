package com.atlassian.labs.remoteapps.api.services;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Asynchronous version of PluginSettings
 */
public interface PluginSettingsAsync
{
    /**
     * Gets a setting value. The setting returned should be specific to this context settings object and not cascade
     * the value to a global context.
     *
     * @param key The setting key.  Cannot be null
     * @return The setting value. May be null
     * @throws IllegalArgumentException if the key is null.
     */
    ListenableFuture<Object> get(String key);

    /**
     * Puts a setting value.
     *
     * @param key   Setting key.  Cannot be null, keys longer than 100 characters are not supported.
     * @param value Setting value.  Must be one of {@link String}, {@link java.util.List}, {@link java.util.Properties}, {@link java.util.Map}, or null.
     *              null will remove the item from the settings.  If the value is a {@link String} it should not be longer
     *              than 99000 characters long.  Values of a type other than {@link String} will be serialized as a
     *              {@link String} which cannot be longer than 99000 characters long.
     * @return The setting value that was over ridden. Null if none existed.
     * @throws IllegalArgumentException if value is not null, {@link String}, {@link java.util.List}, {@link java.util.Properties} or {@link java.util.Map},
     *              or if the key is null or longer than 255 characters
     */
    ListenableFuture<Object> put(String key, Object value);

    /**
     * Removes a setting value
     *
     * @param key The setting key
     * @return The setting value that was removed. Null if nothing was removed.
     * @throws IllegalArgumentException if the key is null.
     */
    ListenableFuture<Object> remove(String key);
}
