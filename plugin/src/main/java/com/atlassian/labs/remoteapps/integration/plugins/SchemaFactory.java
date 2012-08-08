package com.atlassian.labs.remoteapps.integration.plugins;

import com.atlassian.labs.remoteapps.modules.external.Schema;

/**
 * Creates schema instances
 */
public interface SchemaFactory
{
    Schema getSchema();
}
