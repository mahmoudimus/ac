package com.atlassian.plugin.remotable.api.service.http.bigpipe;

import com.atlassian.util.concurrent.Promise;

/**
 * A base interface for BigPipe channels.
 *
 * @since 0.7
 */
// @ThreadSafe
public interface Channel
{
    /**
     * Retains this channel for the lifetime of the specified promise.
     *
     * @param promise The promise to count; must not be null
     * @throws NullPointerException If the promise argument is null
     */
    void retainWhile(Promise<?> promise)
        throws NullPointerException;
}
