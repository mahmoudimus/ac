package com.atlassian.plugin.remotable.sisu;

import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class PostConstructAnnotatedMethodTypeListener extends AbstractAnnotatedMethodTypeListener
{
    public PostConstructAnnotatedMethodTypeListener()
    {
        super(PostConstruct.class);
    }

    @Override
    protected <I> void hear(final Method method, TypeEncounter<I> encounter)
    {
        encounter.register(new InjectionListener<I>()
        {
            public void afterInjection(I injectee)
            {
                try
                {
                    method.invoke(injectee);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
