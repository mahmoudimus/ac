package com.atlassian.labs.remoteapps.api.services;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wraps a service and converts it to its sync or async version via dynamic proxies
 */
public class ServiceWrappers
{
    public static <SYNC, ASYNC> ASYNC wrapAsAsync(SYNC delegate, Class<ASYNC> targetClass)
    {
        return (ASYNC) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass},
                new AsyncWrapper<SYNC>(delegate));
    }

    public static <ASYNC, SYNC> SYNC wrapAsSync(ASYNC delegate, Class<SYNC> targetClass)
    {
        return (SYNC) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass},
                new SyncWrapper<ASYNC>(delegate));
    }

    private static class AsyncWrapper<T> implements InvocationHandler
    {
        private final T delegate;

        /**
         * contructor accepts the real subject
         */
        public AsyncWrapper(T real)
        {
            this.delegate = real;
        }

        /**
         * a generic, reflection-based secure invocation
         */
        public Object invoke(Object target,
                java.lang.reflect.Method method, Object[] arguments)
                throws Throwable
        {
            try
            {
                Class<T> syncClass = (Class<T>) delegate.getClass();
                Method syncMethod = syncClass.getMethod(method.getName(), method.getParameterTypes());
                Object result = syncMethod.invoke(delegate, arguments);
                SettableFuture future = SettableFuture.create();
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

    private static class SyncWrapper<T> implements InvocationHandler
    {
        private final T delegate;

        /**
         * contructor accepts the real subject
         */
        public SyncWrapper(T real)
        {
            this.delegate = real;
        }

        /**
         * a generic, reflection-based secure invocation
         */
        public Object invoke(Object target,
                java.lang.reflect.Method method, Object[] arguments)
                throws Throwable
        {
            try
            {
                Class<T> syncClass = (Class<T>) delegate.getClass();
                Method syncMethod = syncClass.getMethod(method.getName(), method.getParameterTypes());
                ListenableFuture result = (ListenableFuture) syncMethod.invoke(delegate, arguments);
                return result.get();
            }
            catch (java.lang.reflect.InvocationTargetException e)
            {
                // reconvert nested application exceptions
                throw e.getTargetException();
            }
        }
    }
}
