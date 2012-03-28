package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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

    public static String encodeGetUrl(final String iframeSrc, final List<Map.Entry<String, String>> parameters)
    {
        try
        {
            return ContextClassLoaderSwitchingUtil.runInContext(UriBuilder.class.getClassLoader(), new Callable<String>() {
                @Override
                public String call() throws Exception
                {
                    UriBuilder uriBuilder = UriBuilder.fromUri(iframeSrc);
                    for (Map.Entry<String,String> entry : parameters)
                    {
                        String value = entry.getValue() != null ? entry.getValue() : "";
                        // encode slashes as the uri builder turns double slashes into a single slash
                        uriBuilder.queryParam(entry.getKey(), value.replaceAll("/", "%2f"));
                    }
                    return uriBuilder.build().toString();
                }
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException("This should never happen", e);
        }
    }
}
