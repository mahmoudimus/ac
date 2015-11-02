package com.atlassian.plugin.connect.spi.scope;

import com.atlassian.sal.api.user.UserKey;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * An api scope for a given set of functionality
 */
public interface ApiScope
{
    /**
     * Whether to allow the request or not in this scope.
     *
     * @param request the current request. The body can be read repeatedly via {@link javax.servlet.http.HttpServletRequest#getInputStream()}
     * @param user    the logged in user
     * @return {@code true} if allowed
     */
    boolean allow(HttpServletRequest request, @Nullable UserKey user);

    Iterable<ApiResourceInfo> getApiResourceInfos();
}
