package com.atlassian.plugin.connect.spi.util;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

public final class ServletUtils
{
    public static String extractPathInfo(HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        // ACDEV-656
        if (requestURI.contains(";")) {
            requestURI = requestURI.substring(0, requestURI.indexOf(';'));
        }
        return URI.create(requestURI.substring(request.getContextPath().length())).normalize().toString();
    }

    public static boolean normalisedAndOriginalRequestUrisDiffer(HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        String normalizedURI = URI.create(requestURI.substring(request.getContextPath().length())).normalize().toString();

        return !requestURI.equals(normalizedURI);
    }
}
