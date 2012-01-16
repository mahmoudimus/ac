package com.atlassian.labs.remoteapps.sample;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class MyCurrentUserServlet extends HttpServlet
{
    private final OAuthContext oAuthContext;

    public MyCurrentUserServlet(OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = oAuthContext.validate2LOFromParameters(req);

        resp.setContentType("text/plain");
        resp.getWriter().write("this is the incoming url: " + req.getRequestURI());
        resp.getWriter().write("The consumer key is : " + consumerKey);
        resp.getWriter().close();
    }
}
