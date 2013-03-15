package com.atlassian.plugin.remotable.plugin;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import org.dom4j.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Handles permissions for remote plugin operations
 */
public interface PermissionManager
{
    /**
     * Gets the set of permissions for the given installation mode.
     *
     * @param installationMode the installation mode for which we want to gather the allowed permissions
     * @return a set of permissions.
     * @since 0.8
     */
    Set<Permission> getPermissions(InstallationMode installationMode);

    /**
     * Gets the set of permission keys for the given installation mode.
     *
     * @param installationMode the installation mode for which we want to gather the allowed permissions
     * @return a set of permission keys.
     * @since 0.8
     */
    Set<String> getPermissionKeys(InstallationMode installationMode);

    boolean isRequestInApiScope(HttpServletRequest req, String clientKey, String user);

    boolean canInstallRemotePluginsFromMarketplace(String username);

    void requirePermission(String pluginKey, String permissionKey) throws PermissionDeniedException;

    boolean hasPermission(String pluginKey, String permissionKey) throws PermissionDeniedException;

    boolean canModifyRemotePlugin(String username, String pluginKey);

    boolean canRequestDeclaredPermissions(String username, Document descriptor, InstallationMode installationMode);

    boolean canInstallArbitraryRemotePlugins(String userName);
}
