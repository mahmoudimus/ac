package com.atlassian.labs.remoteapps.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import static com.atlassian.labs.remoteapps.sample.HttpUtils.renderHtml;

/**
 *
 */
public class MyMacroServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String pageId = req.getParameter("pageId");
        final String favoriteFooty = req.getParameter("footy");
        final String body = req.getParameter("body");

        renderHtml(resp, "macro.mu", new HashMap<String,Object>() {{
                put("pageId", pageId);
                put("favoriteFooty", favoriteFooty);
                put("body", body);
        }});
    }
}
