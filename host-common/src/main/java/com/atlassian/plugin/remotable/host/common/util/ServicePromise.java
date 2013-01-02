package com.atlassian.plugin.remotable.host.common.util;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServicePromise<T> implements Promise<T>
{
    private final SettableFuture<T> future;
    private final Promise<T> delegate;

    public static <T> T promiseProxy(Promise<T> promise, Class<T> clazz)
    {
        return clazz.cast(
            Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new PromiseInvocationHandler<T>(promise)));
    }

    public ServicePromise(final BundleContext bundleContext, final Class<T> clazz)
    {
        this.future = SettableFuture.create();
        ServiceTracker tracker = new ServiceTracker(bundleContext, clazz.getName(), new ServiceTrackerCustomizer()
        {
            @Override
            public Object addingService(ServiceReference reference)
            {
                future.set(clazz.cast(bundleContext.getService(reference)));
                return null;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service)
            {
            }

            @Override
            public void removedService(ServiceReference reference, Object service)
            {
            }
        });
        this.delegate = Promises.forFuture(future);
        tracker.open();
    }

    @Override
    public T claim()
    {
        return delegate.claim();
    }

    public Promise<T> done(Effect<T> tEffect)
    {
        return delegate.done(tEffect);
    }

    @Override
    public Promise<T> fail(Effect<Throwable> throwableEffect)
    {
        return delegate.fail(throwableEffect);
    }

    public Promise<T> then(FutureCallback<T> tFutureCallback)
    {
        return delegate.then(tFutureCallback);
    }

    public <B> Promise<B> map(Function<? super T, ? extends B> function)
    {
        return delegate.map(function);
    }

    public <B> Promise<B> flatMap(Function<? super T, Promise<B>> promiseFunction)
    {
        return delegate.flatMap(promiseFunction);
    }

    public Promise<T> recover(Function<Throwable, ? extends T> throwableFunction)
    {
        return delegate.recover(throwableFunction);
    }

    public <B> Promise<B> fold(Function<Throwable, ? extends B> throwableFunction, Function<? super T, ? extends B> function)
    {
        return delegate.fold(throwableFunction, function);
    }

    @Override
    public void addListener(Runnable runnable, Executor executor)
    {
        delegate.addListener(runnable, executor);
    }

    @Override
    public boolean cancel(boolean b)
    {
        return delegate.cancel(b);
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
    public T get() throws InterruptedException, ExecutionException
    {
        return delegate.get();
    }

    @Override
    public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return delegate.get(l, timeUnit);
    }

    private static class PromiseInvocationHandler<T> implements InvocationHandler
    {
        private final Promise<T> promise;

        public PromiseInvocationHandler(Promise<T> promise)
        {
            this.promise = promise;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable
        {
            try
            {
                final T instance = promise.claim();
                return method.invoke(instance, objects);
            }
            catch (final InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }
}
