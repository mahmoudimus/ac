package com.atlassian.labs.remoteapps.modules.permissions.scope;

import javax.servlet.http.HttpServletRequest;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope
{
    /**
     * Whether to allow the request or not in this scope
     * @param request The request.  The body can be read repeatedly via getInputStream()
     * @return True if allowed
     */
    boolean allow(HttpServletRequest request);
}
