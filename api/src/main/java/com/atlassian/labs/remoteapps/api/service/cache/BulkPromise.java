package com.atlassian.labs.remoteapps.api.service.cache;

import com.atlassian.util.concurrent.Promise;

import java.util.concurrent.TimeUnit;

/**
 * A promise that allows handling of partial successes
 */
public interface BulkPromise<V> extends Promise<V>
{
    /**
     * @return true if timeout was reached, false otherwise
     */
    public boolean isTimeout();

    /**
     * Wait for the operation to complete and return results
     * <p/>
     * If operation could not complete within specified
     * timeout, partial result is returned. Otherwise, the
     * behavior is identical to {@link #get(long, java.util.concurrent.TimeUnit)}
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     *
     */
    public V claimSome(long timeout, TimeUnit unit);

    /**
     * Gets the status of the operation upon completion.
     *
     * @return the operation status.
     */
    public OperationStatus getStatus();
}
