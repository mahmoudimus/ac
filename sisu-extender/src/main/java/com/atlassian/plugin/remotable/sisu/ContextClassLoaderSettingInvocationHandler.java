package com.atlassian.plugin.remotable.sisu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * InvocationHandler for a dynamic proxy that ensures all methods are executed with the object
 * class's class loader as the context class loader.
 */
final class ContextClassLoaderSettingInvocationHandler implements InvocationHandler
{
    private final Object service;

    ContextClassLoaderSettingInvocationHandler(final Object service)
    {
        this.service = checkNotNull(service);
    }

    public Object invoke(final Object o, final Method method, final Object[] objects) throws Throwable
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader ccl = thread.getContextClassLoader();
        try
        {
            thread.setContextClassLoader(service.getClass().getClassLoader());
            return method.invoke(service, objects);
        }
        catch (final InvocationTargetException e)
        {
            throw e.getTargetException();
        }
        finally
        {
            thread.setContextClassLoader(ccl);
        }
    }
}
