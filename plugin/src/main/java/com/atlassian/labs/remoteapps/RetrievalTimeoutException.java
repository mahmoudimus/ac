package com.atlassian.labs.remoteapps;

/**
 * If the content cannot be retrieved in the given time
 */
public class RetrievalTimeoutException extends ContentRetrievalException
{
    public RetrievalTimeoutException()
    {
        super();
    }

    public RetrievalTimeoutException(String message)
    {
        super(message);
    }

    public RetrievalTimeoutException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RetrievalTimeoutException(Throwable cause)
    {
        super(cause);
    }
}
