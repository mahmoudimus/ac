package com.atlassian.plugin.connect.api.util.http;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Problems when retrieving content from a remote plugin
 */
public final class ContentRetrievalException extends RuntimeException
{
    private final ContentRetrievalErrors errors;

    public ContentRetrievalException(String message)
    {
        this(new ContentRetrievalErrors(ImmutableList.of(message)));
    }

    public ContentRetrievalException(Throwable throwable)
    {
        super(throwable);
        this.errors = new ContentRetrievalErrors(ImmutableList.of(throwable.getMessage()));
    }

    public ContentRetrievalException(String message, Throwable throwable)
    {
        super(message, throwable);
        this.errors = new ContentRetrievalErrors(ImmutableList.of(
                message,
                throwable.getMessage()
        ));
    }

    public ContentRetrievalException(ContentRetrievalErrors errors)
    {
        this.errors = checkNotNull(errors);
    }

    public ContentRetrievalErrors getErrors()
    {
        return errors;
    }
}
