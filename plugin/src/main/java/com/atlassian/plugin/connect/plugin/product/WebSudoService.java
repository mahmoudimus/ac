package com.atlassian.plugin.connect.plugin.product;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebSudoService
{
    void startWebSudoSession(HttpServletRequest request, HttpServletResponse response);

    /**
     * @return the session key used by the host application to mark a session as WebSudo'd (ACDEV-369)
     * @deprecated to be removed once we have solved the permissions vs. scopes issue in Connect
     */
    @Deprecated
    String getWebSudoSessionKey();

}
