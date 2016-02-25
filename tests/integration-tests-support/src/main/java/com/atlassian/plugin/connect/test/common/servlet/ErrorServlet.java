package com.atlassian.plugin.connect.test.common.servlet;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.google.common.collect.ImmutableSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ErrorServlet extends ContextServlet {
    private final int errorCode;
    private final Set<HttpMethod> methods;

    public ErrorServlet(int errorCode, HttpMethod... methods) {
        this.errorCode = errorCode;
        this.methods = ImmutableSet.copyOf(methods.length == 0 ? new HttpMethod[]{HttpMethod.GET} : methods);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        if (methods.contains(HttpMethod.GET)) {
            resp.sendError(errorCode);
        } else {
            throw new UnsupportedOperationException("This servlet does not handle GET requests to " + req.getRequestURI());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        if (methods.contains(HttpMethod.POST)) {
            resp.sendError(errorCode);
        } else {
            throw new UnsupportedOperationException("This servlet does not handle POST requests to " + req.getRequestURI());
        }
    }
}
