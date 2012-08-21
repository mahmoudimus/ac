package com.atlassian.labs.remoteapps.spi.permission;

/**
 *
 */
public final class DefaultPermission implements Permission
{
    private final String key;
    private final String name;
    private final String description;

    public DefaultPermission(String key, String name, String description)
    {
        this.key = key;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }
}
