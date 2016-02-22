package com.atlassian.plugin.connect.test.common.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.api.request.HttpMethod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class MustacheServlet extends ContextServlet {
    private final String templatePath;
    private final Set<HttpMethod> methods;
    private String responseContentType;

    public MustacheServlet(String templatePath, HttpMethod... methods) {
        this.templatePath = templatePath;
        this.methods = ImmutableSet.copyOf(methods.length == 0 ? new HttpMethod[]{HttpMethod.GET} : methods);
    }

    public MustacheServlet(String templatePath, String responseContentType, HttpMethod... methods) {
        this(templatePath, methods);
        this.responseContentType = responseContentType;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        if (methods.contains(HttpMethod.GET)) {
            renderTemplate(resp, context);
        } else {
            throw new UnsupportedOperationException("This servlet does not handle GET requests to " + req.getRequestURI());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        if (methods.contains(HttpMethod.POST)) {
            renderTemplate(resp, context);
        } else {
            throw new UnsupportedOperationException("This servlet does not handle POST requests to " + req.getRequestURI());
        }
    }

    private void renderTemplate(HttpServletResponse resp, Map<String, Object> context) throws IOException {
        if (responseContentType == null) {
            HttpUtils.renderHtml(resp, templatePath, ImmutableMap.copyOf(context));
        } else {
            HttpUtils.renderWithContentType(resp, responseContentType, templatePath, ImmutableMap.copyOf(context));
        }
    }
}
