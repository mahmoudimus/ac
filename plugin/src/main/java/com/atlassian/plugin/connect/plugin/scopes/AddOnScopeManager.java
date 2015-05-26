package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.sal.api.user.UserKey;
import com.google.common.annotations.VisibleForTesting;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles permissions for remote plugin operations
 */
@VisibleForTesting
public interface AddOnScopeManager
{
    /**
     * Tells whether a request can proceed given it's API scope and the plugin requested permissions.
     *
     * @param req the current {@link javax.servlet.http.HttpServletRequest request}
     * @param pluginKey the key of the plugin making the request.
     * @param user the current logged in user
     * @return {@code true} if the request is correctly in the current API scope, {@code false} otherwise
     */
    boolean isRequestInApiScope(HttpServletRequest req, String pluginKey, UserKey user);
}
