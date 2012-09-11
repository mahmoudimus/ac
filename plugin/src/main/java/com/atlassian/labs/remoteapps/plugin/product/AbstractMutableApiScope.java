package com.atlassian.labs.remoteapps.plugin.product;

import com.atlassian.labs.remoteapps.spi.permission.scope.ApiScope;
import com.atlassian.labs.remoteapps.spi.permission.scope.MutablePermission;

/**
 *
 */
abstract class AbstractMutableApiScope implements ApiScope, MutablePermission
{
    private String name;
    private String description;

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
