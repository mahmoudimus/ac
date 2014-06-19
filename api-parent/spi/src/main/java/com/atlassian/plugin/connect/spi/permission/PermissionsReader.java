package com.atlassian.plugin.connect.spi.permission;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import org.dom4j.Document;

import java.util.Set;

/**
 *
 */
@XmlDescriptor
public interface PermissionsReader
{
    Set<String> getPermissionsForPlugin(Plugin plugin);

    Set<String> readPermissionsFromDescriptor(Document document);
}
