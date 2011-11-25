package com.atlassian.labs.remoteapps;

/**
 *
 */
public class PermissionDeniedException extends RuntimeException
{
    public PermissionDeniedException()
    {
    }

    public PermissionDeniedException(String message)
    {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PermissionDeniedException(Throwable cause)
    {
        super(cause);
    }
}
