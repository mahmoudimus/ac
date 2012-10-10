package com.atlassian.labs.remoteapps.api;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;

/**
 * Like {@link com.google.common.util.concurrent.SettableFuture} but with a formalized
 * relationship with {@link Promise}.  A <code>Deferred</code> represents the mutable state
 * of an operation that may execute asynchronously.  Return its promise to clients that
 * are invested in the outcome of the operation's state but that should not be able to
 * mutate that state.
 * <p/>
 * When creating a method that may perform an asynchronous operation, deferred's are
 * typically used as follows:
 * <pre>
 *     public Promise<MyData> getMyDataAsync(String id) {
 *         // create a new Deferred instance to represent this operation
 *         final Deferred<MyData> deferred = Deferred.create();
 *         // perform an async operation to get mydata, typically with a lower-level async api
 *         rawDataClient.get(id)
 *             // handle success case
 *             .done(new Effect<Map<String, Object>>() {
 *                 public void handle(Map<String, Object> value) {
 *                     // convert the result into the target type and resolve the deferred
 *                     deferred.resolve(myDataFromRawData(value));
 *                 }
 *             })
 *             // use Promises.reject to forward failures to this higher-level deferred
 *             .fail(reject(deferred));
 *         // return the deferred's promise
 *         return deferred.promise();
 *     }
 * </pre>
 * Alternatively, sync operations can be presented as async operations by immediately resolving
 * the deferred before returning its promise, like so:
 * <pre>
 *     return Deferred.<MyData>create().resolve(myData).promise();
 * </pre>
 * A convenience method for doing just this is provided for you already, however, as the method
 * <code>Promises.toResolvedPromise(myData)</code>, which is logically equivalent to the above.
 *
 * @see Promises#reject
 * @see Promises#toResolvedPromise
 * @see Promises#toRejectedPromise
 */
public final class Deferred<V> extends AbstractFuture<V> implements Promise<V>
{
    private Promise<V> promise;

    /**
     * Creates a new <code>Deferred</code> instance.
     */
    private Deferred()
    {
        promise = Promises.forListenableFuture(this);
    }

    /**
     * Settles the state of this deferred normally with the specified result value.
     *
     * @param value The result of the represented operation
     * @return This object for fluent chaining
     */
    public Deferred<V> resolve(V value)
    {
        set(value);
        return this;
    }

    /**
     * Settles the state of this deferred with the specified throwable error.
     *
     * @param t The error result of the respresented operation
     * @return This object for fluent chaining
     */
    public Deferred<V> reject(Throwable t)
    {
        setException(t);
        return this;
    }

    /**
     * Returns an immutable propmise for this deferred instance.
     *
     * @return This deferred's promise
     */
    public Promise<V> promise()
    {
        return promise;
    }

    /**
     * Creates a new deferred instance.
     *
     * @return The new instance
     */
    public static <V> Deferred<V> create()
    {
        return new Deferred<V>();
    }

    @Override
    public V claim()
    {
        return promise.claim();
    }

    @Override
    public Deferred<V> onSuccess(final Effect<V> callback)
    {
        promise.onSuccess(callback);
        return this;
    }

    @Override
    public Deferred<V> onFailure(final Effect<Throwable> callback)
    {
        promise.onFailure(callback);
        return this;
    }

    @Override
    public Deferred<V> on(FutureCallback<V> callback)
    {
        promise.on(callback);
        return this;
    }

    @Override
    public <T> Promise<T> map(Function<? super V, ? extends T> function)
    {
        return promise.map(function);
    }

    @Override
    public <T> Promise<T> flatMap(Function<? super V, Promise<T>> function)
    {
        return promise.flatMap(function);
    }
}
