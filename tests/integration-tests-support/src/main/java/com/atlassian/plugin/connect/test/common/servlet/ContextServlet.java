package com.atlassian.plugin.connect.test.common.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

public abstract class ContextServlet
{
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        throw new NotImplementedException("Please override this doGet() in a subclass or do not call it!");
    }

    public void doPost(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        throw new NotImplementedException("Please override this doPost() in a subclass or do not call it!");
    }
}
