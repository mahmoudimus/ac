package com.atlassian.plugin.connect.plugin.module.context;

/**
 * Indicates that the request was malformed in some way
 */
public class MalformedRequestException extends Exception
{
    public MalformedRequestException(String message)
    {
        super(message);
    }

    public MalformedRequestException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
