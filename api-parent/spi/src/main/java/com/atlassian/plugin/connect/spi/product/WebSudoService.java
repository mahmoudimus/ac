package com.atlassian.plugin.connect.spi.product;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebSudoService
{
    /**
     * Start a new WebSudo session. Creates a new {@link javax.servlet.http.HttpSession} if necessary.
     *
     * @param request the current {@link javax.servlet.http.HttpServletRequest}
     * @param response the current {@link javax.servlet.http.HttpServletResponse}
     */
    void startWebSudoSession(HttpServletRequest request, HttpServletResponse response);
}
