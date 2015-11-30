package com.atlassian.plugin.connect.test.common.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;

public final class CustomMessageServlet extends ContextServlet
{
    private final String message;
    private Boolean resize;

    public CustomMessageServlet(String message, Boolean resize)
    {
        this.message = message;
        this.resize = resize;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp,
                "iframe-custom-message.mu",
                ImmutableMap.<String, Object>builder()
                        .putAll(context)
                        .put("message", message)
                        .put("resize", resize)
                        .build());
    }
}
