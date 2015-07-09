package com.atlassian.plugin.connect.plugin.api;

import java.util.Locale;

/**
 * API for checking a license status of an add-on.
 */
public enum LicenseStatus
{
    ACTIVE,
    NONE;

    public String value()
    {
        return toString().toLowerCase(Locale.US);
    }

    public static LicenseStatus fromBoolean(boolean active)
    {
        return active ? LicenseStatus.ACTIVE : LicenseStatus.NONE;
    }
}
