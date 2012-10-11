package com.atlassian.plugin.remotable.spi.schema;

/**
 * Describes a module that will be documented in the XML schema
 */
public interface SchemaDocumented
{
    String getName();

    String getDescription();
}
