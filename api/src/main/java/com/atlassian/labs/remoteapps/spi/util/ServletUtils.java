package com.atlassian.labs.remoteapps.spi.util;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

public final class ServletUtils
{
    public static String extractPathInfo(HttpServletRequest request)
    {
        return URI.create(request.getRequestURI().substring(request.getContextPath().length())).normalize().toString();
    }
}
