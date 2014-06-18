package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import org.dom4j.Document;

import java.net.URL;

/**
 * Accesses the descriptor
 */
public interface DescriptorAccessor
{
    @XmlDescriptor
    Document getDescriptor();

    String getKey();

    URL getDescriptorUrl();
}
