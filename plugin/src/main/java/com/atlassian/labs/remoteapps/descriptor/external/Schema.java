package com.atlassian.labs.remoteapps.descriptor.external;

import org.xml.sax.InputSource;

/**
 *
 */
public interface Schema
{
    String getId();

    String getComplexType();

    String getMaxOccurs();

    InputSource getInputSource();
}
