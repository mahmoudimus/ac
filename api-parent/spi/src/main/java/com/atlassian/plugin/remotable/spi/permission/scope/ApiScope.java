package com.atlassian.plugin.remotable.spi.permission.scope;

import com.atlassian.plugin.remotable.spi.permission.Permission;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope extends Permission
{
    /**
     * Whether to allow the request or not in this scope.
     *
     * @param request the current request. The body can be read repeatedly via {@link HttpServletRequest#getInputStream()}
     * @param user the username of the logged in user
     * @return {@code true} if allowed
     */
    boolean allow(HttpServletRequest request, @Nullable String user);

    Iterable<ApiResourceInfo> getApiResourceInfos();
}
