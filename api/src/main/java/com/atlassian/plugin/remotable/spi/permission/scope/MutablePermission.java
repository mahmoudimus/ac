package com.atlassian.plugin.remotable.spi.permission.scope;

import com.atlassian.plugin.remotable.spi.permission.Permission;

/**
 * Allows the name and description to come from the module descriptor
 */
public interface MutablePermission extends Permission
{
    void setName(String name);
    void setDescription(String description);
}
