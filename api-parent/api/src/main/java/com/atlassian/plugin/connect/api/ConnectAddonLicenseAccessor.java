package com.atlassian.plugin.connect.api;

import com.atlassian.plugin.connect.plugin.api.LicenseStatus;

/**
 * This is an API which is required for Tempo migration to Connect. It works only for Tempo-core plugin.
 *
 * @since 1.1.37
 */
public interface ConnectAddonLicenseAccessor
{

    /**
     * Returns the license status of Connect add-on.
     *
     * @param addonKey key of the add-on to return license of
     * @return ACTIVE if the add-on is licensed, otherwise NONE
     * @since 1.1.37
     */
    LicenseStatus getLicenseStatus(final String addonKey);
}
