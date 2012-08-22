package com.atlassian.labs.remoteapps.spi;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.*;

/**
 * Wraps a ListenableFuture to be a promise
 */
public class WrappingPromise<V> extends ForwardingListenableFuture.SimpleForwardingListenableFuture<V> implements Promise<V>
{
    public WrappingPromise(ListenableFuture<V> delegate)
    {
        super(delegate);
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
                // no-op
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
                // no-op
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
    public Promise<V> then(FutureCallback<V> callback)
    {
        Futures.addCallback(this, callback);
        return this;
    }
}
