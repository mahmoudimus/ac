package com.atlassian.labs.remoteapps.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static com.atlassian.labs.remoteapps.sample.HttpUtils.renderHtml;
import static java.util.Collections.singletonMap;

/**
 *
 */
public class InfoServlet extends HttpServlet
{
    private final String appKey;

    public InfoServlet(String appKey)
    {
        this.appKey = appKey;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        renderHtml(resp, "info-page.mu", Collections.<String, Object>singletonMap("appKey", appKey));

    }
}
