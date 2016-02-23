package com.atlassian.plugin.connect.test.common.servlet.condition;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.common.servlet.ContextServlet;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ParameterCapturingServlet extends ContextServlet {

    private static final Function<String[], String> HEAD_ARRAY = input ->
            input != null && input.length > 0 ? input[0] : null;

    private volatile Map<String, String[]> paramsFromLastRequest;
    private ContextServlet delegate;

    public ParameterCapturingServlet(ContextServlet delegate) {
        this.delegate = delegate;
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        paramsFromLastRequest = req.getParameterMap();
        delegate.doGet(req, resp, context);
    }

    public void doPost(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        paramsFromLastRequest = req.getParameterMap();
        delegate.doGet(req, resp, context);
    }

    public Map<String, String> getParamsFromLastRequest() {
        return Maps.transformValues(paramsFromLastRequest, HEAD_ARRAY);
    }
}