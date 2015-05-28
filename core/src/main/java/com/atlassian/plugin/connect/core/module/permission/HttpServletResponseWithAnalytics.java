package com.atlassian.plugin.connect.core.module.permission;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/*
 * Servlet 2.5 API does not expose the statuscode, this wrapper is needed
 * until we upgrade to servlet 3.0
 */
public class HttpServletResponseWithAnalytics extends HttpServletResponseWrapper
{

    private int statusCode;

    private static final int DEFAULT_STATUS_CODE = SC_OK;

    public HttpServletResponseWithAnalytics(HttpServletResponse response)
    {
        super(response);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        this.setStatusCode(sc);
        super.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        this.setStatusCode(sc);
        super.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        this.setStatusCode(SC_MOVED_PERMANENTLY);
        super.sendRedirect(location);
    }

    @Override
    public void setStatus(int sc)
    {
        this.setStatusCode(sc);
        super.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        this.setStatusCode(sc);
        super.setStatus(sc, sm);
    }

    @Override
    public void reset()
    {
        this.setStatusCode(DEFAULT_STATUS_CODE);
        super.reset();
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

}
