package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.sal.api.user.UserKey;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Handles permissions for remote plugin operations
 */
public interface PermissionManager
{
    /**
     * Gets the set of permissions for the given installation mode.
     *
     * @return a set of permissions
     * @throws NullPointerException if the {@code installationMode} is null.
     * @since 0.8
     */
    @NotNull
    Set<Permission> getPermissions();

    /**
     * Gets the set of permission keys for the given installation mode.
     *
     * @return a set of permission keys
     * @throws NullPointerException if the {@code installationMode} is null.
     * @since 0.8
     */
    @NotNull
    Set<String> getPermissionKeys();

    /**
     * Tells whether a request can proceed given it's API scope and the plugin requested permissions.
     *
     * @param req the current {@link javax.servlet.http.HttpServletRequest request}
     * @param pluginKey the key of the plugin making the request.
     * @param user the current logged in user
     * @return {@code true} if the request is correctly in the current API scope, {@code false} otherwise
     */
    boolean isRequestInApiScope(HttpServletRequest req, String pluginKey, UserKey user);

    /**
     * @deprecated Used only in OAuth and email sending, both of which are themselves deprecated.
     */
    @Deprecated
    void requirePermission(String pluginKey, String permissionKey) throws PermissionDeniedException;
}
