package com.atlassian.plugin.connect.spi.permission;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.scopes.ScopeName;
import org.dom4j.Document;

import java.util.Set;

/**
 *
 */
public interface PermissionsReader
{
    Set<String> getPermissionsForPlugin(Plugin plugin);

    Set<String> readPermissionsFromDescriptor(Document document);

    /**
     * Parse the names of referenced scopes from the {@link Plugin} descriptor of a Connect add-on.
     * @param plugin the Connect add-on
     * @return names of scopes in its descriptor (e.g. "READ", "WRITE")
     */
    Set<ScopeName> readScopesForAddOn(Plugin plugin);
}
