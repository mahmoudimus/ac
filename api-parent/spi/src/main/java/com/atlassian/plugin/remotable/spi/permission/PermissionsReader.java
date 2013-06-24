package com.atlassian.plugin.remotable.spi.permission;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.Plugin;
import org.dom4j.Document;

import java.util.Set;

/**
 *
 */
public interface PermissionsReader
{
    Set<String> getPermissionsForPlugin(Plugin plugin);

    Set<String> readPermissionsFromDescriptor(Document document, InstallationMode installationMode);
}
