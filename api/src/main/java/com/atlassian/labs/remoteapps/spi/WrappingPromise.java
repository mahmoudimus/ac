package com.atlassian.labs.remoteapps.spi;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.*;

/**
 * Wraps a ListenableFuture to be a promise
 */
public class WrappingPromise<V> implements Promise<V>
{
    private final ListenableFuture<V> delegate;

    public WrappingPromise(ListenableFuture<V> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public V claim()
    {
        try
        {
            return get();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            else
            {
                throw new RuntimeException(cause);
            }
        }
    }

    @Override
    public Promise<V> done(final PromiseCallback<V> callback)
    {
        Futures.addCallback(this, new FutureCallback<V>()
        {
            @Override
            public void onSuccess(V result)
            {
                callback.handle(result);
            }

            @Override
            public void onFailure(Throwable t)
            {
            }
        });
        return this;
    }

    @Override
    public Promise<V> fail(final PromiseCallback<Throwable> callback)
    {
        Futures.addCallback(this, new FutureCallback<V>()
        {
            @Override
            public void onSuccess(V result)
            {
            }

            @Override
            public void onFailure(Throwable t)
            {
                callback.handle(t);
            }
        });
        return this;
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
