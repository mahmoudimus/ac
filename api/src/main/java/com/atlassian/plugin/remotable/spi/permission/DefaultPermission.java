package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.spi.permission.scope.MutablePermission;

/**
 *
 */
public final class DefaultPermission implements MutablePermission
{
    private final String key;
    private String name;
    private String description;

    public DefaultPermission(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
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
