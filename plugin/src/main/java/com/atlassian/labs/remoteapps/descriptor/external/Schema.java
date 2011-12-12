package com.atlassian.labs.remoteapps.descriptor.external;

import org.dom4j.Document;
import org.xml.sax.InputSource;

/**
 *
 */
public interface Schema
{
    String getId();

    String getComplexType();

    String getMaxOccurs();

    Document getDocument();
}
