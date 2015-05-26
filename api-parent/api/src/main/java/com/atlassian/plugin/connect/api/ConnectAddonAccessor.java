package com.atlassian.plugin.connect.api;

import com.atlassian.plugin.connect.plugin.api.LicenseStatus;

/**
 * Service which checks if Connect add-on is enabled.
 * @since 1.1.31
 */
public interface ConnectAddonAccessor
{

    /**
     * Checks if the Connect add-on is installed and enabled.
     *
     * @param addonKey key of the add-on to check
     * @return true if the Connect add-on is installed and enabled, otherwise false
     */
    boolean isAddonEnabled(String addonKey);

    /**
     * Returns the license status of Connect add-on
     *
     * @param addonKey key of the add-on to return license of
     * @return ACTIVE if the add-on is licensed, otherwise NONE
     */
    LicenseStatus getLicenseStatus(String addonKey);
}
