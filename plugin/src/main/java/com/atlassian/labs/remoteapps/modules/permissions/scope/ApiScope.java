package com.atlassian.labs.remoteapps.modules.permissions.scope;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface ApiScope
{
    boolean allow(HttpServletRequest request);
}
