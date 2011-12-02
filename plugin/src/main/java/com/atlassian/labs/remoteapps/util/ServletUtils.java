package com.atlassian.labs.remoteapps.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
public class ServletUtils
{
    public static String extractPathInfo(HttpServletRequest request)
    {
        return URI.create(request.getRequestURI().substring(request.getContextPath().length())).normalize().toString();
    }

    public static String encodeGetUrl(String iframeSrc, Map<String, String> parameters)
    {
        return encodeGetUrl(iframeSrc, newArrayList(parameters.entrySet()));
    }

    public static String encodeGetUrl(String iframeSrc, List<Map.Entry<String, String>> parameters)
    {

        UriBuilder uriBuilder = UriBuilder.fromUri(iframeSrc);
        for (Map.Entry<String,String> entry : parameters)
        {
            // encode slashes as the uri builder turns double slashes into a single slash
            uriBuilder.queryParam(entry.getKey(), entry.getValue().replaceAll("/", "%2f"));
        }
        return uriBuilder.build().toString();
    }
}
