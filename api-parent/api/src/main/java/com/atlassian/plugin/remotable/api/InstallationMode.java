package com.atlassian.plugin.remotable.api;

import com.atlassian.fugue.Option;

public enum InstallationMode
{
    REMOTE("remote"),
    LOCAL("local"),
    CONTAINER("container");

    private final String key;

    private InstallationMode(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    /**
     * Parses the installation mode from its name in a case insensitive manner.
     *
     * @param name the name of the installation mode to parse
     * @return a defined option of the installation mode if it was resolved, otherwise none.
     * @since 0.8
     */
    public static Option<InstallationMode> of(String name)
    {
        for (InstallationMode mode : values())
        {
            if (mode.getKey().equalsIgnoreCase(name))
            {
                return Option.some(mode);
            }
        }
        return Option.none();
    }
}
