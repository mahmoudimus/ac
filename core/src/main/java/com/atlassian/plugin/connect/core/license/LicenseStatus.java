package com.atlassian.plugin.connect.core.license;

import java.util.Locale;

/**
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
