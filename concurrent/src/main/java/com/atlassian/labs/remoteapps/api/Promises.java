package com.atlassian.labs.remoteapps.api;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.List;

import static java.util.Arrays.*;

/**
 * Helper methods for working with promises
 */
public final class Promises
{
    private Promises() { }

    /**
     * Returns a new promise representing the status of a list of other promises.
     *
     * @param promises The promises that the new promise should track
     * @return The new, aggregate promise
     */
    public static <V> Promise<List<V>> when(Promise<? extends V>... promises)
    {
        return when(asList(promises));
    }

    /**
     * Returns a new promise representing the status of a list of other promises.
     *
     * @param promises The promises that the new promise should track
     * @return The new, aggregate promise
     */
    public static <V> Promise<List<V>> when(Iterable<? extends Promise<? extends V>> promises)
    {
        return toPromise(Futures.<V>allAsList(promises));
    }

    /**
     * Creates a new, resolved promise for the specified concrete value.
     *
     * @param instance The value for which a promise should be created
     * @return The new promise
     */
    public static <V> Promise<V> toResolvedPromise(V instance)
    {
        return new StaticPromise<V>(instance);
    }

    /**
     * Creates a new, rejected promise from the given Throwable and result type.
     *
     * @param instance The throwable
     * @param resultType The result type
     * @return The new promise
     */
    public static <V> Promise<V> toRejectedPromise(Throwable instance, Class<V> resultType)
    {
        return new StaticPromise<V>(instance);
    }

    /**
     * Creates a promise from the given future.
     *
     * @param future The future delegte for the new promise
     * @return The new promise
     */
    public static <V> Promise<V> toPromise(ListenableFuture<V> future)
    {
        return new WrappingPromise<V>(future);
    }

    /**
     * Transforms a promise from one type to another by way of a transformation function.
     *
     * @param promise The promise to transform
     * @param function THe transformation function
     * @return The promise resulting from the transformation
     */
    public static <I, O> Promise<O> transform(Promise<I> promise, Function<? super I, ? extends O> function)
    {
        return toPromise(Futures.transform(promise, function));
    }

    /**
     * Creates a new <code>PromiseCallback</code> that forwards a promise's fail events to
     * the specified future delegate's <code>setException</code> method -- that is, the new
     * callback rejects the delegate future if invoked.
     *
     * @param delegate The future to be rejected on a fail event
     * @return The fail callback
     */
    public static PromiseCallback<Throwable> reject(final SettableFuture<?> delegate)
    {
        return new PromiseCallback<Throwable>()
        {
            @Override
            public void handle(Throwable t)
            {
                delegate.setException(t);
            }
        };
    }
}
