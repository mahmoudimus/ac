package com.atlassian.plugin.remotable.spi.schema;

import org.dom4j.Document;

/**
 *
 */
public interface Schema extends SchemaDocumented
{
    String getFileName();

    String getElementName();

    String getName();

    String getDescription();

    String getComplexType();

    String getMaxOccurs();

    Iterable<String> getRequiredPermissions();

    Iterable<String> getOptionalPermissions();

    Document getDocument();
}
