package com.atlassian.labs.remoteapps.util;

import net.oauth.OAuthMessage;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 *
 */
public class ServletUtils
{
    public static String extractPathInfo(HttpServletRequest request)
    {
        return URI.create(request.getRequestURI().substring(request.getContextPath().length())).normalize().toString();
    }

    public static String encodeIFrameSrc(String iframeSrc, OAuthMessage message)
    {

        UriBuilder uriBuilder = UriBuilder.fromUri(iframeSrc);
        try
        {
            for (Map.Entry<String,String> entry : message.getParameters())
            {
                // encode slashes as the uri builder turns double slashes into a single slash
                uriBuilder.queryParam(entry.getKey(), entry.getValue().replaceAll("/", "%2f"));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return uriBuilder.build().toString();
    }
}
