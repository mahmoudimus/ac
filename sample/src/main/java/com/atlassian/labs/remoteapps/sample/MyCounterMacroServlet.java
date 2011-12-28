package com.atlassian.labs.remoteapps.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.atlassian.labs.remoteapps.sample.HttpServer.getOurBaseUrl;

/**
 *
 */
public class MyCounterMacroServlet extends HttpServlet
{
    private int counter = 1;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.print("<div class=\"rp-counter\">" + counter++ + "</div>");
        writer.close();
    }
}
