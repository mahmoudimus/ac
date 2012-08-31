package com.atlassian.labs.remoteapps.api;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A promise that presents a nicer interface to {@link java.util.concurrent.Future}
 */
public interface Promise<V> extends ListenableFuture<V>
{
    /**
     * Blocks the thread waiting for a result.  Exceptions are thrown as runtime exceptions.
     *
     * @return The promised object
     */
    V claim();

    /**
     * Registers a callback to be called when the promised object is available.  May not be executed
     * in the same thread as the caller.
     *
     * @param callback The callback
     * @return This object for chaining
     */
    Promise<V> done(PromiseCallback<V> callback);

    /**
     * Registers a callback to be called when an exception is thrown.  May not be executed
     * in the same thread as the caller.
     *
     * @param callback The callback
     * @return This object for chaining
     */
    Promise<V> fail(PromiseCallback<Throwable> callback);

    /**
     * Registers a FutureCallback to handle both done (success) and fail (exception) cases.
     * May not be executed in the same thread as the caller.
     *
     * @param callback The future callback
     * @return This object for chaining
     */
    Promise<V> then(FutureCallback<V> callback);
}
