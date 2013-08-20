package com.atlassian.plugin.connect.api.service.http.bigpipe;

import com.atlassian.fugue.Option;

/**
 * An injectable manager providing access to BigPipe and ConsumableBigPipe instances.
 *
 * @since 0.7
 */
public interface BigPipeManager
{
    /**
     * Gets the BigPipe instance for the current request, creating it if it doesn't already exist, for
     * providers of big pipe content.
     *
     * @return A BigPipe instance for the current request; will never be null
     * @throws IllegalStateException If called from a thread lacking an assigned requestId
     */
    BigPipe getBigPipe();

    /**
     * Return a ConsumableBigPipe instance for the current request for consumers of available big pipe content.
     * Only contains an instance when it has pending or available content.
     *
     * @return An option of a ConsumableBigPipe instance for the current request
     */
    Option<ConsumableBigPipe> getConsumableBigPipe();
}
