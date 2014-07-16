package com.atlassian.plugin.connect.plugin.rest;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestError
{
    @JsonProperty
    private final int errorCode;

    @JsonProperty
    private final String message;

    public RestError(@JsonProperty("errorCode") final int errorCode,
            @JsonProperty("message") final String message)
    {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public String getMessage()
    {
        return message;
    }
}
