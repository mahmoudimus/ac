package com.atlassian.labs.remoteapps.modules.external;

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

    Document getDocument();
}
