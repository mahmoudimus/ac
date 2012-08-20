package com.atlassian.labs.remoteapps.spi.permission;

import com.atlassian.labs.remoteapps.spi.schema.SchemaDocumented;

/**
 *
 */
public interface Permission extends SchemaDocumented
{
    String getKey();
}
