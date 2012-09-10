package com.atlassian.labs.remoteapps.spi;

import com.atlassian.labs.remoteapps.api.Promise;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public final class Promises
{
    private Promises() {}

    public static <V> Promise<V> ofInstance(V instance)
    {
        return new StaticPromise<V>(instance);
    }

    public static <V> Promise<V> ofThrowable(Throwable instance, Class<V> resultType)
    {
        return new StaticPromise<V>(instance);
    }

    public static <V> Promise<V> ofFuture(ListenableFuture<V> future)
    {
        return new WrappingPromise<V>(future);
    }

    public static <I, O> Promise<O> transform(Promise<I> promise, Function<? super I, ? extends O> function)
    {
        return ofFuture(Futures.transform(promise, function));
    }
}
