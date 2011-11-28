package com.atlassian.labs.remoteapps.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class MyMacroServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String pageId = req.getParameter("pageId");
        String favoriteFooty = req.getParameter("footy");
        String body = req.getParameter("body");

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.print("<p>Page ID: <span class=\"rp-page-id\">" + pageId + "</span></p>");
        writer.print("<p>Favorite Footy: <span class=\"rp-footy\">" + favoriteFooty + "</span></p>");
        writer.print("<p>Body: <div  class=\"rp-body\">" + body + "</div></p>");
        writer.close(); 
    }
}
