package com.atlassian.labs.remoteapps.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class MySlowMacroServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            Thread.sleep(12 * 1000);
        }
        catch (InterruptedException e)
        {
            // do nothing
        }
        resp.setContentType("text/html");
        resp.getWriter().write("finished");
        resp.getWriter().close();
    }
}
