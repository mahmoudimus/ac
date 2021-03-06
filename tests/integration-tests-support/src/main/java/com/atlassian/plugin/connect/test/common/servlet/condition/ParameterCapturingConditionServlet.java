package com.atlassian.plugin.connect.test.common.servlet.condition;

import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ParameterCapturingConditionServlet extends HttpServlet {
    /**
     * The suggested (though not mandatory) route for this servlet.
     */
    public static final String PARAMETER_CAPTURE_URL = "/parameterCapture";

    private static final Function<String[], String> HEAD_ARRAY = input ->
            input != null && input.length > 0 ? input[0] : null;

    private volatile Map<String, String[]> paramsFromLastRequest;

    private volatile Map<String, String> headersFromLastRequest;

    private volatile String conditionReturnValue;

    public ParameterCapturingConditionServlet() {
        this.conditionReturnValue = "true";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        paramsFromLastRequest = req.getParameterMap();
        Map<String, String> headers = new HashMap<>();

        for (String name : Collections.list((Enumeration<String>) req.getHeaderNames())) {
            headers.put(name, req.getHeader(name));
        }

        headersFromLastRequest = headers;

        resp.setContentType("application/json");
        resp.getWriter().write("{\"shouldDisplay\" : " + conditionReturnValue + "}");
        resp.getWriter().close();
    }

    public Option<String> getHttpHeaderFromLastRequest(final String name) {
        Predicate<String> equalsIgnoreCase = headerName -> headerName.equalsIgnoreCase(name);

        Option<String> maybeHeaderName = Iterables.findFirst(this.headersFromLastRequest.keySet(), equalsIgnoreCase);
        return maybeHeaderName.flatMap(headerName -> Option.option(headersFromLastRequest.get(headerName)));
    }

    public Map<String, String> getParamsFromLastRequest() {
        return Maps.transformValues(paramsFromLastRequest, HEAD_ARRAY);
    }

    public void clearParams() {
        paramsFromLastRequest = null;
    }

    public void setConditionReturnValue(String conditionReturnValue) {
        this.conditionReturnValue = conditionReturnValue;
    }
}