package com.atlassian.labs.remoteapps.spi;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class StaticPromise<V> implements Promise<V>
{
    private final Promise<V> delegate;

    public StaticPromise(V value)
    {
        SettableFuture<V> future = SettableFuture.create();
        future.set(value);
        delegate = Promises.ofFuture(future);
    }

    @Override
    public V claim()
    {
        return delegate.claim();
    }

    public Promise<V> done(PromiseCallback<V> callback)
    {
        return delegate.done(callback);
    }

    @Override
    public Promise<V> fail(PromiseCallback<Throwable> callback)
    {
        return delegate.fail(callback);
    }

    public Promise<V> then(FutureCallback<V> callback)
    {
        return delegate.then(callback);
    }

    @Override
    public void addListener(Runnable listener, Executor executor)
    {
        delegate.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled()
    {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return delegate.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException
    {
        return delegate.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException
    {
        return delegate.get(timeout, unit);
    }
}
