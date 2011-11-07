package com.atlassian.labs.remoteapps.util;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 *
 */
public class ServletUtils
{
    public static String extractPathInfo(HttpServletRequest request)
    {
        return URI.create(request.getRequestURI().substring(request.getContextPath().length())).normalize().toString();
    }
}
