package com.atlassian.labs.remoteapps.modules.external;

import org.dom4j.Document;

/**
 *
 */
public interface Schema
{
    String getId();

    String getTitle();

    String getDescription();

    String getComplexType();

    String getMaxOccurs();

    Document getDocument();
}
