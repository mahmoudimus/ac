package com.atlassian.plugin.connect.spi.permission;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.xmldescriptor.XmlDescriptor;
import org.dom4j.Document;

import java.util.Set;

/**
 *
 */
public interface PermissionsReader
{
    @XmlDescriptor
    Set<String> getPermissionsForPlugin(Plugin plugin);

    @XmlDescriptor
    Set<String> readPermissionsFromDescriptor(Document document);

    /**
     * Parse the names of referenced scopes from the {@link Plugin} descriptor of a Connect add-on.
     * @param plugin the Connect add-on
     * @return names of scopes in its descriptor (e.g. "READ", "WRITE")
     */
    Set<ScopeName> readScopesForAddOn(Plugin plugin);
}
