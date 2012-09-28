package com.atlassian.labs.remoteapps.host.common.service.http;

import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;

/**
 * Handler for a settable future that allows us to wrap calls to set and setException with whatever
 * wrapping logic we need, like transfering request context information.
 *
 */
public interface SettableFutureHandler<V>
{
    /**
     * Sets the value of this future.  This method will return {@code true} if
     * the value was successfully set, or {@code false} if the future has already
     * been set or cancelled.
     *
     * @param value the value the future should hold.
     * @return true if the value was successfully set.
     */
    boolean set(@Nullable V value);

    /**
     * Sets the future to having failed with the given exception. This exception
     * will be wrapped in an {@code ExecutionException} and thrown from the {@code
     * get} methods. This method will return {@code true} if the exception was
     * successfully set, or {@code false} if the future has already been set or
     * cancelled.
     *
     * @param throwable the exception the future should hold.
     * @return true if the exception was successfully set.
     */
    boolean setException(Throwable throwable);

    /**
     * @return The underlying future
     */
    SettableFuture<V> getFuture();
}
