package com.atlassian.plugin.connect.plugin.module.common.user;

public class UserLookupException extends RuntimeException
{
    public UserLookupException(Throwable cause)
    {
        super(cause);
    }

    public UserLookupException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
