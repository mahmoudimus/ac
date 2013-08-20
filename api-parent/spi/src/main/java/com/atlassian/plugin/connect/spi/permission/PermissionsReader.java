package com.atlassian.plugin.connect.spi.permission;

import java.util.Set;

import com.atlassian.plugin.Plugin;

import org.dom4j.Document;

/**
 *
 */
public interface PermissionsReader
{
    Set<String> getPermissionsForPlugin(Plugin plugin);

    Set<String> readPermissionsFromDescriptor(Document document);
}
