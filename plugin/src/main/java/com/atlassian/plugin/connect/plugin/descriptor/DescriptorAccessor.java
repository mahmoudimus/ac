package com.atlassian.plugin.connect.plugin.descriptor;

import java.net.URL;

import org.dom4j.Document;

/**
 * Accesses the descriptor
 */
public interface DescriptorAccessor
{
    Document getDescriptor();

    String getKey();

    URL getDescriptorUrl();
}
