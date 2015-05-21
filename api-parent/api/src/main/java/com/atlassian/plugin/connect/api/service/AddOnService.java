package com.atlassian.plugin.connect.api.service;

/**
 * Service which checks if Connect add-on is enabled.
 * @since 1.1.31
 */
public interface AddOnService
{
    /**
     * Checks if the Connect add-on is installed.
     *
     * @param connectAddOnKey key of the add-on to check
     * @return true if the Connect add-on is enabled, otherwise false
     */
    boolean isAddOnEnabled(String connectAddOnKey);
}
