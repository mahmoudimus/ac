package com.atlassian.plugin.connect.core.rest;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public class RestResult
{
    @JsonProperty("status-code")
    private final int statusCode;

    @JsonProperty("message")
    private final String message;

    public RestResult(@JsonProperty ("status-code") final int statusCode,
            @JsonProperty ("message") final String message)
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
