package com.atlassian.plugin.connect.test.common.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;

public class MustacheServlet extends ContextServlet
{
    private final String templatePath;
    private final boolean shouldHandlePost;

    public MustacheServlet(String templatePath)
    {
        this(templatePath, false);
    }

    public MustacheServlet(String templatePath, boolean shouldHandlePost)
    {
        this.templatePath = templatePath;
        this.shouldHandlePost = shouldHandlePost;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        renderTemplate(resp, context);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        if (shouldHandlePost)
        {
            renderTemplate(resp, context);
        }
    }

    private void renderTemplate(HttpServletResponse resp, Map<String, Object> context) throws IOException
    {
        HttpUtils.renderHtml(resp, templatePath, ImmutableMap.copyOf(context));
    }
}
