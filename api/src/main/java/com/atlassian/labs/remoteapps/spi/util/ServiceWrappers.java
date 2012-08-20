package com.atlassian.labs.remoteapps.spi.util;

import com.google.common.util.concurrent.SettableFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.*;

/**
 * Wraps a service and converts it to its sync or async version via dynamic proxies
 */
public final class ServiceWrappers
{
    private ServiceWrappers()
    {
    }

    @SuppressWarnings("unchecked")
    public static <SYNC, ASYNC> ASYNC wrapAsAsync(SYNC delegate, Class<ASYNC> targetClass)
    {
        return (ASYNC) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class[]{targetClass}, new AsyncWrapper<SYNC>(delegate));
    }

    @SuppressWarnings("unchecked")
    public static <ASYNC, SYNC> SYNC wrapAsSync(ASYNC delegate, Class<SYNC> targetClass)
    {
        return (SYNC) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class[]{targetClass}, new SyncWrapper<ASYNC>(delegate));
    }

    private static final class AsyncWrapper<T> implements InvocationHandler
    {
        private final T delegate;

        /**
         * contructor accepts the real subject
         */
        public AsyncWrapper(T syncDelegate)
        {
            this.delegate = checkNotNull(syncDelegate);
        }

        /**
         * a generic, reflection-based secure invocation
         */
        public Object invoke(Object target, java.lang.reflect.Method method, Object[] arguments) throws Throwable
        {
            try
            {
                final Method syncMethod = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
                final Object result = syncMethod.invoke(delegate, arguments);

                final SettableFuture future = SettableFuture.create();
                future.set(result);
                return future;
            }
            catch (java.lang.reflect.InvocationTargetException e)
            {
                // reconvert nested application exceptions
                throw e.getTargetException();
            }
        }
    }

    private static final class SyncWrapper<T> implements InvocationHandler
    {
        private final T asyncDelegate;

        /**
         * contructor accepts the real subject
         */
        public SyncWrapper(T asyncDelegate)
        {
            this.asyncDelegate = checkNotNull(asyncDelegate);
        }

        /**
         * a generic, reflection-based secure invocation
         */
        public Object invoke(Object target, java.lang.reflect.Method method, Object[] arguments) throws Throwable
        {
            try
            {
                final Class asyncClass = asyncDelegate.getClass();
                final Method syncMethod = asyncClass.getMethod(method.getName(), method.getParameterTypes());

                final Object result = syncMethod.invoke(asyncDelegate, arguments);
                if (Future.class.isAssignableFrom(result.getClass())) // most cases
                {
                    return ((Future) result).get();
                }
                else // toString, equals, etc.
                {
                    return result;
                }
            }
            catch (java.lang.reflect.InvocationTargetException e)
            {
                // reconvert nested application exceptions
                throw e.getTargetException();
            }
        }
    }
}
