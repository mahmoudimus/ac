package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;

public class InvalidContextParameterException extends MalformedRequestException
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
