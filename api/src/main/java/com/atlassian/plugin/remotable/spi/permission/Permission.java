package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.spi.schema.SchemaDocumented;

/**
 *
 */
public interface Permission extends SchemaDocumented
{
    String getKey();
}
