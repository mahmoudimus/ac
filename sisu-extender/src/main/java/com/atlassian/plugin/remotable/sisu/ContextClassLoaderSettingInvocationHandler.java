package com.atlassian.plugin.remotable.sisu;

import com.atlassian.fugue.Option;
import com.google.common.base.Supplier;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.memoize;

/**
 * InvocationHandler for a dynamic proxy that ensures all methods are executed with the object
 * class's class loader as the context class loader.
 */
final class ContextClassLoaderSettingInvocationHandler implements InvocationHandler
{
    private final Supplier<Object> service;
    private final Option<String> toString;

    ContextClassLoaderSettingInvocationHandler(final Supplier<Object> service, Option<String> toString)
    {
        this.service = memoize(checkNotNull(service));
        this.toString = checkNotNull(toString);
    }

    public Object invoke(final Object o, final Method method, final Object[] objects) throws Throwable
    {
        if (toString.isDefined() && isToString(method))
        {
            return toString.get();
        }

        return invokeOnService(method, objects);
    }

    private Object invokeOnService(Method method, Object[] objects) throws Throwable
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader ccl = thread.getContextClassLoader();
        try
        {
            thread.setContextClassLoader(service.get().getClass().getClassLoader());
            return method.invoke(service.get(), objects);
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

    private boolean isToString(Method method)
    {
        return method != null
                && method.getName().equals("toString")
                && method.getParameterTypes().length == 0; // no parameters
    }
}
