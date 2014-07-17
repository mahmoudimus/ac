package com.atlassian.plugin.connect.plugin.rest;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestError
{
    @JsonProperty("status-code")
    private final int statusCode;

    @JsonProperty("message")
    private final String message;

    public RestError(@JsonProperty("status-code") final int statusCode,
            @JsonProperty("message") final String message)
    {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getMessage()
    {
        return message;
    }
}
