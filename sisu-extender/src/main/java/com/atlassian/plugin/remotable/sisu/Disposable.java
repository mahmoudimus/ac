package com.atlassian.plugin.remotable.sisu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class Disposable
{
    final Method method;
    final Object object;

    public Disposable(Method method, Object object)
    {
        this.method = method;
        this.object = object;
    }

    void dispose() throws DisposeException
    {
        try
        {
            method.invoke(object);
        }
        catch (IllegalAccessException e)
        {
            throw new DisposeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new DisposeException(e);
        }
        catch (Exception e)
        {
            throw new DisposeException(e);
        }
    }
}
