package com.atlassian.plugin.connect.test.common.servlet;

import com.atlassian.plugin.connect.test.common.util.NameValuePairs;
import com.google.common.collect.ImmutableMap;
import org.apache.http.NameValuePair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class EchoQueryParametersServlet extends MustacheServlet {
    private volatile BlockingDeque<NameValuePairs> queryParameters = new LinkedBlockingDeque<>();

    public EchoQueryParametersServlet() {
        this("echo-query.mu");
    }

    public EchoQueryParametersServlet(String templatePath) {
        super(templatePath);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        Map<String, String[]> parameterMap = getParameterMap(req);
        NameValuePairs parameters = new NameValuePairs(parameterMap);
        queryParameters.push(parameters);

        List<Map<String, String>> nvps = new ArrayList<>();

        for (NameValuePair pair : parameters.getNameValuePairs()) {
            HashMap<String, String> nvp = new HashMap<>();

            nvp.put("name", pair.getName());
            nvp.put("value", pair.getValue());
            nvps.add(nvp);
        }

        Map<String, Object> newContext = new HashMap<String, Object>();
        newContext.putAll(context);

        newContext.put("nvp", nvps);

        super.doGet(req, resp, ImmutableMap.copyOf(newContext));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> getParameterMap(HttpServletRequest req) {
        return req.getParameterMap();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException {
        doGet(req, resp, context);
    }

    public NameValuePairs waitForQueryParameters() throws InterruptedException {
        return queryParameters.poll(5, TimeUnit.SECONDS);
    }
}
