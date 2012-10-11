package com.atlassian.plugin.remotable.host.common.descriptor;

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
