package com.atlassian.plugin.connect.spi.permission.scope;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.spi.permission.Permission;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope extends Permission
{
    /**
     * Whether to allow the request or not in this scope.
     *
     * @param request the current request. The body can be read repeatedly via {@link HttpServletRequest#getInputStream()}
     * @param user    the username of the logged in user
     * @return {@code true} if allowed
     */
    boolean allow(HttpServletRequest request, @Nullable String user);

    Iterable<ApiResourceInfo> getApiResourceInfos();
}
