package com.atlassian.labs.remoteapps.plugin;

/**
 * Problems when retrieving content from a remote app
 */
public class ContentRetrievalException extends RuntimeException
{
    public ContentRetrievalException()
    {
    }

    public ContentRetrievalException(String message)
    {
        super(message);
    }

    public ContentRetrievalException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ContentRetrievalException(Throwable cause)
    {
        super(cause);
    }
}
