package com.atlassian.plugin.connect.core.iframe.context;

public class InvalidContextParameterException extends RuntimeException
{
    public InvalidContextParameterException(String message)
    {
        super(message);
    }

    public InvalidContextParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
