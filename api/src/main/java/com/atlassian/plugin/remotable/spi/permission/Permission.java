package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.schema.spi.SchemaDocumented;

/**
 *
 */
public interface Permission extends SchemaDocumented
{
    String getKey();
}
