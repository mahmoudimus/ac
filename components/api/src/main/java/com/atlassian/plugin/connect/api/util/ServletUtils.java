package com.atlassian.plugin.connect.api.util;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

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
        String requestURI = request.getRequestURI().substring(request.getContextPath().length());
        String normalizedURI = URI.create(requestURI).normalize().toString();

        return !requestURI.equals(normalizedURI);
    }
}
