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

    public MustacheServlet(String templatePath)
    {
        this.templatePath = templatePath;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp, templatePath, ImmutableMap.copyOf(context));
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        // do nothing
    }
}
