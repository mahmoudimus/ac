package com.atlassian.labs.remoteapps.sample;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class MyImageMacroServlet extends HttpServlet
{
    private final OAuthContext oAuthContext;

    public MyImageMacroServlet(OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.print("<img src=\"" + oAuthContext.getLocalBaseUrl() + "/sandcastles.jpg\" alt=\"sandcastles\"/>");
        writer.close(); 
    }
}
