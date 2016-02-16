package com.atlassian.plugin.connect.test.common.servlet;

import javax.servlet.http.HttpServletRequest;

public class FormParameterExtractor
{
    private final String parameterId;

    public FormParameterExtractor(final String parameterId)
    {
        this.parameterId = parameterId;
    }

    public String getParameterId()
    {
        return parameterId;
    }

    public String extract(HttpServletRequest request)
    {
        return request.getParameter(parameterId);
    }
}
