package com.atlassian.plugin.connect.test.common.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class ResourceServlet extends ContextServlet
{
    private final String resourcePath;
    private final String contentType;

    public ResourceServlet(String resourcePath, String contentType)
    {
        this.resourcePath = resourcePath;
        this.contentType = contentType;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        InputStream resourceAsStream = null;
        try
        {
            resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (null == resourceAsStream)
            {
                resp.sendError(404);
            }
            else
            {
                resp.setContentType(contentType);
                IOUtils.copy(resourceAsStream, resp.getOutputStream());
            }
        }
        finally
        {
           IOUtils.closeQuietly(resourceAsStream);
        }
    }
}
