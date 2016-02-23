package com.atlassian.plugin.connect.plugin.auth.scope;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles access to API methods for add-ons.
 */
public interface AddonScopeManager {

    /**
     * Tells whether a request can proceed given it's API scope and the plugin requested permissions.
     *
     * @param request the current {@link HttpServletRequest request}
     * @param addonKey the key of the add-on making the request.
     * @return {@code true} if the request is correctly in the current API scope, {@code false} otherwise
     * @throws IllegalStateException if an add-on with the given key is not installed
     */
    boolean isRequestInApiScope(HttpServletRequest request, String addonKey);
}
