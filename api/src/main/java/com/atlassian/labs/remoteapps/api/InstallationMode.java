package com.atlassian.labs.remoteapps.api;

/**
 *
 */
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
}
