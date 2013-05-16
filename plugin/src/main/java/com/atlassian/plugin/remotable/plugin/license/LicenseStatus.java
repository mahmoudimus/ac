package com.atlassian.plugin.remotable.plugin.license;

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
