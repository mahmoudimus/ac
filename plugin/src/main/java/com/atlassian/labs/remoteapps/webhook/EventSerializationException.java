package com.atlassian.labs.remoteapps.webhook;

/**
 *
 */
public class EventSerializationException extends RuntimeException
{
    public EventSerializationException()
    {
    }

    public EventSerializationException(String message)
    {
        super(message);
    }

    public EventSerializationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public EventSerializationException(Throwable cause)
    {
        super(cause);
    }
}
