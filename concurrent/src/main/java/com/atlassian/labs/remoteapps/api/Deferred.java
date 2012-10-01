package com.atlassian.labs.remoteapps.api;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;

public final class Deferred<V> extends AbstractFuture<V> implements Promise<V>
{
    private Promise<V> promise;

    public Deferred()
    {
        this.promise = new WrappingPromise<V>(this);
    }

    public Deferred<V> resolve(V value)
    {
        set(value);
        return this;
    }

    public Deferred<V> reject(Throwable t)
    {
        setException(t);
        return this;
    }

    public Promise<V> promise()
    {
        return promise;
    }

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
    public Deferred<V> done(final PromiseCallback<V> callback)
    {
        promise.done(callback);
        return this;
    }

    @Override
    public Deferred<V> fail(final PromiseCallback<Throwable> callback)
    {
        promise.fail(callback);
        return this;
    }

    @Override
    public Deferred<V> then(FutureCallback<V> callback)
    {
        promise.then(callback);
        return this;
    }
}
