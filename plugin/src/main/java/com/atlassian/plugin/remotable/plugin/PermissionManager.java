package com.atlassian.plugin.remotable.plugin;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import org.dom4j.Document;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles permissions for remote plugin operations
 */
public interface PermissionManager
{
    Iterable<Permission> getPermissions();

    boolean isRequestInApiScope(HttpServletRequest req, String clientKey, String user);

    boolean canInstallRemotePluginsFromMarketplace(String username);

    void requirePermission(String pluginKey, String permissionKey) throws PermissionDeniedException;

    boolean hasPermission(String pluginKey, String permissionKey) throws PermissionDeniedException;

    boolean canModifyRemotePlugin(String username, String pluginKey);

    boolean canRequestDeclaredPermissions(String username, Document descriptor, InstallationMode installationMode);

    boolean canInstallArbitraryRemotePlugins(String userName);
}
