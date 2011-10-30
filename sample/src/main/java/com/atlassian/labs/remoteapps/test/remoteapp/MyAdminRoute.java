package com.atlassian.labs.remoteapps.test.remoteapp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class MyAdminRoute extends RemoteAppFilter.Route
{
    public MyAdminRoute(String path)
    {
        super(path);
    }

    private Map<String, Object> getRequestMap(HttpServletRequest req)
    {
        Map<String,Object> result = newHashMap();
        for (String key : ((Collection<String>)req.getParameterMap().keySet()))
        {
            result.put(key, req.getParameter(key));
        }
        return result;
    }

    @Override
    String handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        resp.setContentType("text/html");
        String consumerKey = OAuthContext.INSTANCE.validate2LOFromParameters(req);
        final Map<String, Object> context = getRequestMap(req);
        context.put("consumerKey", consumerKey);
        return render("test-page.vm", context);
    }
}
