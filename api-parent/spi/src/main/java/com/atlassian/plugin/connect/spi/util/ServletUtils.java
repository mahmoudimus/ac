package com.atlassian.plugin.connect.spi.util;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

public final class ServletUtils
{
    public static String extractPathInfo(HttpServletRequest request)
    {
        return URI.create(request.getRequestURI().substring(request.getContextPath().length())).normalize().toString();
    }
}
