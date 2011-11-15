package com.atlassian.labs.remoteapps.installer;

/**
 *
 */
public enum AccessLevel
{
    GLOBAL,
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
