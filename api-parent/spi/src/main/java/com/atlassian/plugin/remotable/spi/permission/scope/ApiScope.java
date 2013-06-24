package com.atlassian.plugin.remotable.spi.permission.scope;

import com.atlassian.plugin.remotable.spi.permission.Permission;

import javax.servlet.http.HttpServletRequest;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope extends Permission
{
    /**
     * Whether to allow the request or not in this scope
     *
     * @param request The request.  The body can be read repeatedly via getInputStream()
     * @param user The logged in user name
     * @return True if allowed
     */
    boolean allow(HttpServletRequest request, String user);

    Iterable<ApiResourceInfo> getApiResourceInfos();
}
