package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;

import java.net.URL;

/**
 * Accesses the descriptor
 */
public interface DescriptorAccessor
{
    Document getDescriptor();

    String getKey();

    URL getDescriptorUrl();
}
