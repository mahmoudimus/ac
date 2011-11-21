package com.atlassian.labs.remoteapps.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.labs.remoteapps.sample.HttpUtils.renderHtml;


/**
 *
 */
public class MyAdminServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = OAuthContext.INSTANCE.validate2LOFromParameters(req);
        final Map<String, Object> context = new HashMap<String,Object>();
        context.put("consumerKey", consumerKey);
        context.put("baseurl", HttpServer.getHostBaseUrl());
        renderHtml(resp, "test-page.mu", context);
    }

}
