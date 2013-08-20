package com.atlassian.plugin.connect.plugin.license;

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
}
