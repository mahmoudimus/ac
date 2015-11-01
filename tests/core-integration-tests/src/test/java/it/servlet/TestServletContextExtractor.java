package it.servlet;

import javax.servlet.http.HttpServletRequest;

public final class TestServletContextExtractor
{
    private final String parameterId;

    public TestServletContextExtractor(final String parameterId)
    {
        this.parameterId = parameterId;
    }

    public String getParameterId()
    {
        return parameterId;
    }

    public Object extract(HttpServletRequest request)
    {
        return request.getParameter(parameterId);
    }
}
