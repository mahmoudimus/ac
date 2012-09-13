package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.labs.remoteapps.spi.schema.SchemaDocumented;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;

import java.net.URI;
import java.util.Map;

/**
 * A remote module generator.  This is used to convert one part of a remote app descriptor into plugin XML to be
 * stored in a generated local plugin.
 */
public interface RemoteModuleGenerator extends SchemaDocumented
{
    String getType();

    Schema getSchema();

    void validate(Element element, URI registrationUrl, String username) throws PluginParseException;

    void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot);
}
