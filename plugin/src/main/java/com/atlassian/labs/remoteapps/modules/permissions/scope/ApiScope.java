package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.modules.external.SchemaDocumented;

import javax.servlet.http.HttpServletRequest;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope extends SchemaDocumented
{
    String getKey();

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
