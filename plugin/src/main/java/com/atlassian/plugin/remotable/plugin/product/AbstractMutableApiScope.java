package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.MutablePermission;

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
