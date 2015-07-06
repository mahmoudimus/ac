package com.atlassian.plugin.connect.api;

/**
 * Service which checks if Connect add-on is enabled.
 * @since 1.1.32
 */
public interface ConnectAddonAccessor
{
    /**
     * Checks if the Connect add-on is installed and enabled.
     *
     * @param addOnKey key of the add-on to check
     * @return true if the Connect add-on is installed and enabled, otherwise false
     */
    boolean isAddonEnabled(String addOnKey);

}
