package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.spi.http.HttpMethod;

/**
 * Specify the HTTP method to use when calling the static content macro.
 *
 * @schemaTitle Macro HTTP Method
 * @since 1.0
 */
public enum MacroHttpMethod
{
    GET(HttpMethod.GET),
    POST(HttpMethod.POST),
    PUT(HttpMethod.PUT);

    private final HttpMethod method;

    private MacroHttpMethod(HttpMethod method)
    {
        this.method = method;
    }

    public HttpMethod getMethod()
    {
        return method;
    }
}
