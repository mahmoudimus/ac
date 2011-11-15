package com.atlassian.labs.remoteapps.installer;

/**
 * The access level for a remote app
 */
public enum AccessLevel
{
    /** The remote app is immediately available globally */
    GLOBAL,

    /** The remote app is available, but has to be enabled by individual users to be active (piggybacks on speakeasy) */
    PER_USER;

    public static AccessLevel parse(String val)
    {
        if (val == null || "global".equals(val))
        {
            return GLOBAL;
        }
        else if ("user".equals(val))
        {
            return PER_USER;
        }
        throw new IllegalArgumentException("Invalid access level: '" + val + "'");
    }
}
