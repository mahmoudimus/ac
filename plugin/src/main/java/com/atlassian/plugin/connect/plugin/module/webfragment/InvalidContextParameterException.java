package com.atlassian.plugin.connect.plugin.module.webfragment;

public class InvalidContextParameterException extends Exception
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
