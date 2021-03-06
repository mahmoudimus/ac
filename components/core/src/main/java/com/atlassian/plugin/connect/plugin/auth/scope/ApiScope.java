package com.atlassian.plugin.connect.plugin.auth.scope;

import javax.servlet.http.HttpServletRequest;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope {
    /**
     * Whether to allow the request or not in this scope.
     *
     * @param request the current request. The body can be read repeatedly via {@link javax.servlet.http.HttpServletRequest#getInputStream()}
     * @return {@code true} if allowed
     */
    boolean allow(HttpServletRequest request);

    Iterable<ApiResourceInfo> getApiResourceInfos();
}
