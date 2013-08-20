package com.atlassian.plugin.connect.api.service.http.bigpipe;

import com.atlassian.util.concurrent.Promise;

/**
 * A handle to a named BigPipe data channel.
 *
 * @since 0.7
 */
public interface DataChannel extends Channel
{
    /**
     * Registers a data string promise on the named channel.  Use of this method will generate client-side BigPipe
     * events delivering the promised strings to subscribers by channelId.
     *
     * @param stringPromise A promise for the data content string; must not be null
     * @throws NullPointerException If stringPromise is null
     */
    void promiseContent(Promise<String> stringPromise) throws NullPointerException;
}
